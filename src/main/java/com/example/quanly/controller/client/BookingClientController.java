package com.example.quanly.controller.client;

import com.example.quanly.domain.*;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.domain.dto.AvailableTimeDTO;
import com.example.quanly.domain.dto.BookingResponseDTO;
import com.example.quanly.domain.dto.HoldBookingRequest;
import com.example.quanly.domain.PaymentType;
import com.example.quanly.domain.dto.PaymentRequest;
import com.example.quanly.domain.dto.PlaceBookingRequest;
import com.example.quanly.domain.dto.ProductResponseDTO;
import jakarta.validation.Valid;
import com.example.quanly.domain.dto.VnpayResponse;
import com.example.quanly.repository.*;
import com.example.quanly.service.BookingService;
import com.example.quanly.service.PaymentService;
import com.example.quanly.service.ProductService;
import com.example.quanly.service.RacketService;
import com.example.quanly.service.RecommendationService;
import com.example.quanly.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/client/bookings")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BookingClientController {

    ProductService productService;
    RacketService racketService;
    SubCourtRepository subCourtRepository;
    TimeRepository timeRepository;
    BookingDetailRepository bookingDetailRepository;
    BookingService bookingService;
    PaymentService paymentService;
    TemporaryBookingRepository temporaryBookingRepository;
    RecommendationService recommendationService;
    SecurityUtils securityUtils;

    @GetMapping("/recommend/{productId}")
    public ResponseEntity<ApiResponse<List<AvailableTimeDTO>>> getRecommendations(
            @PathVariable Long productId) {

        User user = getCurrentUser();
        List<AvailableTimeDTO> recs = recommendationService.recommendSlots(user.getId(), productId);
        return ResponseEntity.ok(ApiResponse.<List<AvailableTimeDTO>>builder()
                .status(200).message("Gợi ý cho bạn").data(recs).build());
    }

    @GetMapping("/{productId}/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBookingInfo(
            @PathVariable long productId) {

        ProductResponseDTO product = productService.getProductByID(productId);
        List<AvailableTime> allTimes = productService.getAllTime();
        List<SubCourt> courts = productService.getAllCourtsByProduct(productId);

        double price = product.getPrice();
        double totalPrice = price - (price * product.getSale() / 100.0);

        Map<String, Object> data = Map.of(
                "product", product,
                "courts", courts,
                "availableTimes", allTimes,
                "totalPrice", totalPrice);

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thành công").data(data).build());
    }

    @GetMapping("/available-times")
    public ResponseEntity<ApiResponse<List<AvailableTimeDTO>>> getAvailableTimes(
            @RequestParam("date") String dateStr,
            @RequestParam("courtId") Long courtId) {

        LocalDate date = LocalDate.parse(dateStr);
        SubCourt court = subCourtRepository.findById(courtId).orElse(null);

        List<BookingDetail> bookings = bookingDetailRepository.findBySubCourtAndDate(court, date);
        Set<Long> bookedTimeIds = bookings.stream()
                .map(b -> b.getAvailableTime().getId())
                .collect(Collectors.toSet());

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        log.info("Date: {}, CourtId: {}", date, courtId);

        List<AvailableTimeDTO> result = timeRepository.findAll().stream()
                .filter(time -> {
                    if (date.equals(today) && time.getTime().isBefore(now)) return false;
                    return !bookedTimeIds.contains(time.getId());
                })
                .map(AvailableTimeDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<AvailableTimeDTO>>builder()
                .status(200).message("Thành công").data(result).build());
    }

    @PostMapping("/hold")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> holdCourt(
            @Valid @RequestBody HoldBookingRequest holdRequest) {

        User currentUser = getCurrentUser();
        Long userId = currentUser.getId();

        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(3);
        temporaryBookingRepository.deleteExpiredHolds(expiryTime);
        temporaryBookingRepository.flush();

        SubCourt court = subCourtRepository.findById(holdRequest.getSubCourtId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sân phụ"));
        AvailableTime time = timeRepository.findById(holdRequest.getAvailableTimeId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khung giờ"));

        Optional<TemporaryBooking> existingOpt = temporaryBookingRepository
                .findBySubCourtAndAvailableTimeAndBookingDateWithLock(court, time, holdRequest.getBookingDate());

        LocalDateTime now = LocalDateTime.now();

        if (existingOpt.isPresent()) {
            TemporaryBooking existing = existingOpt.get();
            if (existing.isExpired()) {
                temporaryBookingRepository.delete(existing);
                temporaryBookingRepository.flush();
            } else {
                if (!existing.getUserId().equals(userId)) {
                    long elapsed = Duration.between(existing.getHoldStartTime(), now).getSeconds();
                    long remaining = Math.max(180 - elapsed, 0);
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse
                            .<Map<String, Object>>builder()
                            .status(409)
                            .message("Khung giờ này đang được giữ. Vui lòng thử lại sau.")
                            .data(Map.of("remainingTime", remaining))
                            .build());
                } else {
                    existing.setHoldStartTime(now);
                    temporaryBookingRepository.save(existing);
                    return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                            .status(200).message("Tiếp tục giữ sân tạm thời")
                            .data(Map.of("remainingTime", 180)).build());
                }
            }
        }

        TemporaryBooking newHold = new TemporaryBooking();
        newHold.setSubCourt(court);
        newHold.setAvailableTime(time);
        newHold.setBookingDate(holdRequest.getBookingDate());
        newHold.setUserId(userId);
        newHold.setHoldStartTime(now);
        temporaryBookingRepository.save(newHold);

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Giữ sân tạm thời thành công")
                .data(Map.of("remainingTime", 180)).build());
    }

    @PostMapping("/place")
    public ResponseEntity<ApiResponse<Map<String, Object>>> placeBooking(
            @Valid @RequestBody PlaceBookingRequest req,
            HttpServletRequest request) {

        User currentUser = getCurrentUser();

        BookingResponseDTO booking = bookingService.handlePlaceBooking(currentUser,
                req.getReceiverName(), req.getReceiverAddress(), req.getReceiverPhone(),
                req.getProductId(), req.getAvailableTimeId(), req.getCourtId(), req.getBookingDate(),
                req.getBookingType(), req.getRecurringEndDate());

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setId(booking.getId());
        paymentRequest.setAmount(booking.getDepositPrice());
        paymentRequest.setType(PaymentType.BOOKING);
        paymentRequest.setRedirectUrl("");

        VnpayResponse vnpayResponse = paymentService.createVnPayPayment(paymentRequest, request);

        Map<String, Object> data = Map.of(
                "bookingId", booking.getId(),
                "bookingCode", booking.getBookingCode(),
                "paymentUrl", vnpayResponse.getPaymentUrl());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<Map<String, Object>>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Đặt sân thành công").data(data).build());
    }

    @GetMapping("/{bookingCode}/{courtId}/rackets")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBookingRackets(
            @PathVariable String bookingCode,
            @PathVariable long courtId) {

        List<Racket> racketList = racketService.getAvailableRacketsByCourt(courtId);
        Map<String, Object> data = Map.of(
                "rackets", racketList,
                "bookingCode", bookingCode);
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thành công").data(data).build());
    }

    private User getCurrentUser() {
        return securityUtils.getCurrentUser();
    }
}
