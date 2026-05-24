package com.example.quanly.controller.client;

import com.example.quanly.domain.*;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.exception.ResourceNotFoundException;
import com.example.quanly.domain.dto.AvailableTimeDTO;
import com.example.quanly.domain.dto.HoldBookingRequest;
import com.example.quanly.domain.dto.PreparedBookingResult;
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
import com.example.quanly.service.SlotEventPublisher;
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
    SlotEventPublisher slotEventPublisher;

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
        SubCourt court = subCourtRepository.findById(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân phụ ID: " + courtId));

        List<BookingDetail> bookings = bookingDetailRepository.findBySubCourtAndDate(court, date);
        Set<Long> bookedTimeIds = bookings.stream()
                .map(b -> b.getAvailableTime().getId())
                .collect(Collectors.toSet());

        // Lấy danh sách các khung giờ đang bị giữ tạm thời và chưa hết hạn
        List<TemporaryBooking> temporaryBookings = temporaryBookingRepository.findBySubCourtAndBookingDate(court, date);
        Set<Long> heldTimeIds = temporaryBookings.stream()
                .filter(tb -> !tb.isExpired())
                .map(tb -> tb.getAvailableTime().getId())
                .collect(Collectors.toSet());

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        log.info("Date: {}, CourtId: {}", date, courtId);

        List<AvailableTimeDTO> result = timeRepository.findAll().stream()
                .filter(time -> {
                    if (date.equals(today) && time.getTime().isBefore(now))
                        return false;
                    // Không hiển thị nếu đã đặt chính thức HOẶC đang bị giữ chỗ
                    return !bookedTimeIds.contains(time.getId()) && !heldTimeIds.contains(time.getId());
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
                    slotEventPublisher.publishHeld(court.getId(), time.getId(),
                            holdRequest.getBookingDate(), userId, now.plusMinutes(3));
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

        slotEventPublisher.publishHeld(court.getId(), time.getId(),
                holdRequest.getBookingDate(), userId, now.plusMinutes(3));

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Giữ sân tạm thời thành công")
                .data(Map.of("remainingTime", 180)).build());
    }

    @PostMapping("/place")
    public ResponseEntity<ApiResponse<Map<String, Object>>> placeBooking(
            @Valid @RequestBody PlaceBookingRequest req,
            HttpServletRequest request) {

        User currentUser = getCurrentUser();

        PreparedBookingResult prepared = bookingService.preparePendingBooking(currentUser,
                req.getReceiverName(), req.getReceiverAddress(), req.getReceiverPhone(),
                req.getProductId(), req.getAvailableTimeId(), req.getCourtId(), req.getBookingDate(),
                req.getBookingType(), req.getRecurringEndDate(), req.getRackets(), req.getWeekdays());

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setId(prepared.pendingId());
        paymentRequest.setAmount(prepared.depositPrice());
        paymentRequest.setType(PaymentType.PENDING_BOOKING);
        paymentRequest.setRedirectUrl("");

        VnpayResponse vnpayResponse = paymentService.createVnPayPayment(paymentRequest, request);

        Map<String, Object> data = Map.of("paymentUrl", vnpayResponse.getPaymentUrl());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<Map<String, Object>>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Vui lòng hoàn tất thanh toán để xác nhận đặt sân").data(data).build());
    }

    @GetMapping("/products/{productId}/rackets")
    public ResponseEntity<ApiResponse<List<Racket>>> getRacketsByProduct(@PathVariable Long productId) {
        List<Racket> rackets = racketService.getRacketsByProductId(productId);
        return ResponseEntity.ok(ApiResponse.<List<Racket>>builder()
                .status(200).message("Thành công").data(rackets).build());
    }

    @lombok.experimental.NonFinal
    @org.springframework.beans.factory.annotation.Value("${app.contact.hotline:0123456789}")
    private String contactHotline;
    @lombok.experimental.NonFinal
    @org.springframework.beans.factory.annotation.Value("${app.contact.email:admin@badmintonhub.vn}")
    private String contactEmail;

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<com.example.quanly.domain.dto.CancelBookingResponse>> cancelBooking(
            @PathVariable long id,
            @org.springframework.web.bind.annotation.RequestBody(required = false)
                    com.example.quanly.domain.dto.CancelBookingRequest req) {
        User currentUser = getCurrentUser();
        String reason = req != null ? req.getReason() : null;
        BookingService.CancelResult result = bookingService.cancelByUser(id, currentUser.getId(), reason);

        com.example.quanly.domain.dto.CancelBookingResponse body =
                com.example.quanly.domain.dto.CancelBookingResponse.builder()
                        .bookingId(result.bookingId)
                        .bookingCode(result.bookingCode)
                        .status("DA_HUY")
                        .refundStatus(result.refundStatus != null ? result.refundStatus.name() : null)
                        .refundAmount(result.refundAmount)
                        .usedSessions(result.usedSessions)
                        .totalSessions(result.totalSessions)
                        .contactHotline(contactHotline)
                        .contactEmail(contactEmail)
                        .message(result.refundAmount > 0
                                ? String.format("Bạn sẽ được hoàn %,.0f VNĐ tiền cọc. Nếu chưa nhận được, vui lòng liên hệ admin.", result.refundAmount)
                                : "Đơn đã huỷ. Không có khoản cọc nào được hoàn lại.")
                        .build();

        return ResponseEntity.ok(ApiResponse.<com.example.quanly.domain.dto.CancelBookingResponse>builder()
                .status(200).message("Huỷ đặt sân thành công").data(body).build());
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
