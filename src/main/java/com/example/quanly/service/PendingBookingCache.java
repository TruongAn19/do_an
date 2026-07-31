package com.example.quanly.service;

import com.example.quanly.domain.dto.PendingBookingData;
import com.example.quanly.domain.dto.PendingBookingClaim;
import com.example.quanly.domain.PendingBookingPayment;
import com.example.quanly.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PendingBookingCache {
    private static final int TTL_MINUTES = 20;

    private final PendingBookingPaymentRepository repository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final TimeRepository timeRepository;
    private final SubCourtRepository subCourtRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public long store(PendingBookingData data) {
        try {
            PendingBookingPayment row = new PendingBookingPayment();
            row.setTemporaryBookingId(data.getTemporaryBookingId());
            row.setUserId(data.getUser().getId());
            row.setProductId(data.getProduct().getId());
            row.setAvailableTimeId(data.getAvailableTime().getId());
            row.setSubCourtId(data.getSubCourt().getId());
            row.setReceiverName(data.getReceiverName());
            row.setReceiverAddress(data.getReceiverAddress());
            row.setReceiverPhone(data.getReceiverPhone());
            row.setFirstBookingDate(data.getFirstBookingDate());
            row.setBookingType(data.getBookingType());
            row.setRecurringEndDate(data.getRecurringEndDate());
            row.setTotalBookingPrice(data.getTotalBookingPrice());
            row.setDepositPrice(data.getDepositPrice());
            row.setSlotsJson(objectMapper.writeValueAsString(data.getSlots()));
            row.setRentalSlotsJson(objectMapper.writeValueAsString(data.getRentalSlots()));
            row.setTemporaryBookingIdsJson(objectMapper.writeValueAsString(data.getTemporaryBookingIds()));
            row.setExpiresAt(LocalDateTime.now().plusMinutes(TTL_MINUTES));
            return repository.save(row).getId();
        } catch (Exception e) {
            throw new IllegalStateException("Không thể lưu phiên thanh toán đặt sân.", e);
        }
    }

    @Transactional
    public Optional<PendingBookingData> get(long id) {
        Optional<PendingBookingPayment> found = repository.findById(id);
        if (found.isEmpty()) return Optional.empty();
        PendingBookingPayment row = found.get();
        if (row.getExpiresAt().isBefore(LocalDateTime.now())) {
            repository.delete(row);
            return Optional.empty();
        }
        try {
            List<PendingBookingData.SlotData> slots = objectMapper.readValue(
                    row.getSlotsJson(), new TypeReference<>() {});
            List<PendingBookingData.RentalSlot> rentals = objectMapper.readValue(
                    row.getRentalSlotsJson(), new TypeReference<>() {});
            List<Long> holdIds = readHoldIds(row);
            return Optional.of(new PendingBookingData(
                    row.getTemporaryBookingId(),
                    userRepository.findById(row.getUserId()).orElseThrow(),
                    row.getReceiverName(), row.getReceiverAddress(), row.getReceiverPhone(),
                    productRepository.findById(row.getProductId()).orElseThrow(),
                    timeRepository.findById(row.getAvailableTimeId()).orElseThrow(),
                    subCourtRepository.findById(row.getSubCourtId()).orElseThrow(),
                    row.getFirstBookingDate(), row.getBookingType(), row.getRecurringEndDate(),
                    row.getTotalBookingPrice(), row.getDepositPrice(), slots, rentals, holdIds));
        } catch (Exception e) {
            throw new IllegalStateException("Không thể đọc phiên thanh toán đặt sân.", e);
        }
    }

    @Transactional
    public Optional<PendingBookingClaim> claim(long id) {
        Optional<PendingBookingPayment> found = repository.findByIdForUpdate(id);
        if (found.isEmpty()) return Optional.empty();
        PendingBookingPayment row = found.get();
        if (row.getExpiresAt().isBefore(LocalDateTime.now())) {
            repository.delete(row);
            return Optional.empty();
        }
        if ("COMPLETED".equals(row.getProcessingStatus())) {
            return Optional.of(new PendingBookingClaim(
                    null, true, row.getCompletedBookingId(), row.getCompletedBookingCode()));
        }
        PendingBookingData data = toData(row);
        row.setProcessingStatus("PROCESSING");
        repository.save(row);
        return Optional.of(new PendingBookingClaim(data, false, null, null));
    }

    @Transactional
    public void markCompleted(long id, Long bookingId, String bookingCode) {
        PendingBookingPayment row = repository.findByIdForUpdate(id).orElseThrow();
        row.setProcessingStatus("COMPLETED");
        row.setCompletedBookingId(bookingId);
        row.setCompletedBookingCode(bookingCode);
        row.setExpiresAt(LocalDateTime.now().plusHours(24));
        repository.save(row);
    }

    @Transactional
    public void remove(long id) {
        repository.deleteById(id);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void removeExpired() {
        repository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    private PendingBookingData toData(PendingBookingPayment row) {
        try {
            List<PendingBookingData.SlotData> slots = objectMapper.readValue(
                    row.getSlotsJson(), new TypeReference<>() {});
            List<PendingBookingData.RentalSlot> rentals = objectMapper.readValue(
                    row.getRentalSlotsJson(), new TypeReference<>() {});
            List<Long> holdIds = readHoldIds(row);
            return new PendingBookingData(
                    row.getTemporaryBookingId(),
                    userRepository.findById(row.getUserId()).orElseThrow(),
                    row.getReceiverName(), row.getReceiverAddress(), row.getReceiverPhone(),
                    productRepository.findById(row.getProductId()).orElseThrow(),
                    timeRepository.findById(row.getAvailableTimeId()).orElseThrow(),
                    subCourtRepository.findById(row.getSubCourtId()).orElseThrow(),
                    row.getFirstBookingDate(), row.getBookingType(), row.getRecurringEndDate(),
                    row.getTotalBookingPrice(), row.getDepositPrice(), slots, rentals, holdIds);
        } catch (Exception e) {
            throw new IllegalStateException("Không thể đọc phiên thanh toán đặt sân.", e);
        }
    }

    private List<Long> readHoldIds(PendingBookingPayment row) throws Exception {
        if (row.getTemporaryBookingIdsJson() == null || row.getTemporaryBookingIdsJson().isBlank()) {
            return row.getTemporaryBookingId() == null
                    ? List.of()
                    : List.of(row.getTemporaryBookingId());
        }
        return objectMapper.readValue(row.getTemporaryBookingIdsJson(), new TypeReference<>() {});
    }
}
