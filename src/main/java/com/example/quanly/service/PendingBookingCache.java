package com.example.quanly.service;

import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.Product;
import com.example.quanly.domain.Racket;
import com.example.quanly.domain.SubCourt;
import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.PendingBookingData;
import com.example.quanly.domain.dto.PendingBookingSnapshot;
import com.example.quanly.exception.ResourceNotFoundException;
import com.example.quanly.repository.ProductRepository;
import com.example.quanly.repository.RacketRepository;
import com.example.quanly.repository.SubCourtRepository;
import com.example.quanly.repository.TimeRepository;
import com.example.quanly.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Persists pending-booking data to Redis with a TTL covering the VNPay payment window.
 * Survives app restarts and is shared across instances (unlike the previous in-memory map).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PendingBookingCache {

    /**
     * TTL = cùng cửa sổ với DB hold ({@link BookingService#PAYMENT_HOLD_WINDOW}) cộng buffer
     * nhỏ cho độ trễ callback VNPay. Phải ≥ hold window: nếu Redis hết hạn trước DB hold,
     * callback sẽ không tìm thấy snapshot và user mất tiền không có booking.
     */
    private static final Duration TTL = BookingService.PAYMENT_HOLD_WINDOW.plus(Duration.ofMinutes(2));
    private static final String KEY_PREFIX = "pending-booking:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper baseObjectMapper;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SubCourtRepository subCourtRepository;
    private final TimeRepository timeRepository;
    private final RacketRepository racketRepository;

    private ObjectMapper jsonMapper;

    @PostConstruct
    void initMapper() {
        // Defensive copy: register JavaTimeModule even if global ObjectMapper missed it.
        this.jsonMapper = baseObjectMapper.copy().registerModule(new JavaTimeModule());
    }

    public long store(PendingBookingData data) {
        PendingBookingSnapshot snapshot = toSnapshot(data);
        String json;
        try {
            json = jsonMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Không thể serialize pending booking", e);
        }

        long id;
        String key;
        // SETNX loop guarantees a unique key even across instances.
        do {
            id = ThreadLocalRandom.current().nextLong(1_000_000L, Long.MAX_VALUE);
            key = KEY_PREFIX + id;
        } while (Boolean.FALSE.equals(redisTemplate.opsForValue().setIfAbsent(key, json, TTL)));

        log.debug("Lưu pending booking vào Redis: pendingId={}, ttl={}", id, TTL);
        return id;
    }

    public Optional<PendingBookingData> get(long pendingId) {
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + pendingId);
        if (json == null) {
            return Optional.empty();
        }
        try {
            PendingBookingSnapshot snapshot = jsonMapper.readValue(json, PendingBookingSnapshot.class);
            return Optional.of(rehydrate(snapshot));
        } catch (JsonProcessingException e) {
            log.error("Không thể deserialize pending booking pendingId={}: {}", pendingId, e.getMessage());
            return Optional.empty();
        }
    }

    public void remove(long pendingId) {
        redisTemplate.delete(KEY_PREFIX + pendingId);
        log.debug("Xóa pending booking khỏi Redis: pendingId={}", pendingId);
    }

    private PendingBookingSnapshot toSnapshot(PendingBookingData data) {
        List<PendingBookingSnapshot.SlotSnapshot> slots = new ArrayList<>();
        if (data.getSlots() != null) {
            for (PendingBookingData.SlotData s : data.getSlots()) {
                slots.add(new PendingBookingSnapshot.SlotSnapshot(s.getDate(), s.getPrice(), s.getSale()));
            }
        }
        List<PendingBookingSnapshot.RentalSnapshot> rentals = new ArrayList<>();
        if (data.getRentals() != null) {
            for (PendingBookingData.RentalSlot r : data.getRentals()) {
                rentals.add(new PendingBookingSnapshot.RentalSnapshot(
                        r.getRacket().getId(), r.getQuantity(), r.getUnitPrice(), r.getSubtotal()));
            }
        }
        return new PendingBookingSnapshot(
                data.getTemporaryBookingIds(),
                data.getUser().getId(),
                data.getReceiverName(),
                data.getReceiverAddress(),
                data.getReceiverPhone(),
                data.getProduct().getId(),
                data.getAvailableTime().getId(),
                data.getSubCourt().getId(),
                data.getFirstBookingDate(),
                data.getBookingType(),
                data.getRecurringEndDate(),
                data.getTotalBookingPrice(),
                data.getDepositPrice(),
                slots,
                rentals);
    }

    private PendingBookingData rehydrate(PendingBookingSnapshot s) {
        User user = userRepository.findById(s.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng id=" + s.getUserId()));
        Product product = productRepository.findById(s.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm id=" + s.getProductId()));
        SubCourt subCourt = subCourtRepository.findById(s.getSubCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân phụ id=" + s.getSubCourtId()));
        AvailableTime time = timeRepository.findById(s.getAvailableTimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khung giờ id=" + s.getAvailableTimeId()));

        List<PendingBookingData.SlotData> slots = new ArrayList<>();
        if (s.getSlots() != null) {
            for (PendingBookingSnapshot.SlotSnapshot ss : s.getSlots()) {
                slots.add(new PendingBookingData.SlotData(ss.getDate(), ss.getPrice(), ss.getSale()));
            }
        }
        List<PendingBookingData.RentalSlot> rentals = new ArrayList<>();
        if (s.getRentals() != null) {
            for (PendingBookingSnapshot.RentalSnapshot rs : s.getRentals()) {
                Racket racket = racketRepository.findById(rs.getRacketId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vợt id=" + rs.getRacketId()));
                rentals.add(new PendingBookingData.RentalSlot(racket, rs.getQuantity(), rs.getUnitPrice(), rs.getSubtotal()));
            }
        }

        return new PendingBookingData(
                s.getTemporaryBookingIds(), user,
                s.getReceiverName(), s.getReceiverAddress(), s.getReceiverPhone(),
                product, time, subCourt,
                s.getFirstBookingDate(), s.getBookingType(), s.getRecurringEndDate(),
                s.getTotalBookingPrice(), s.getDepositPrice(),
                slots, rentals);
    }
}
