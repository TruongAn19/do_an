package com.example.quanly.controller;

import com.example.quanly.domain.PaymentType;
import com.example.quanly.domain.RentalTool;
import com.example.quanly.domain.RentalToolStatus;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.domain.dto.BookingResponseDTO;
import com.example.quanly.domain.dto.PendingBookingData;
import com.example.quanly.repository.RentalToolRepository;
import com.example.quanly.service.BookingService;
import com.example.quanly.service.EmailService;
import com.example.quanly.service.PaymentService;
import com.example.quanly.service.PendingBookingCache;
import com.example.quanly.service.RentalToolService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PaymentController {

    PaymentService paymentService;
    RentalToolService rentalToolService;
    RentalToolRepository rentalToolRepository;
    BookingService bookingService;
    PendingBookingCache pendingBookingCache;
    EmailService emailService;

    /**
     * GET /api/v1/payments/vnpay-callback
     *
     * OrderInfo format: "{id}-{TYPE}"
     *   PENDING_BOOKING: id is the in-memory pendingId (nothing saved to DB before payment)
     *   RENTAL_TOOL:     id is the RentalTool DB id
     */
    @GetMapping("/vnpay-callback")
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleVnpayCallback(HttpServletRequest request) {
        if (!paymentService.verifyVnpayCallback(request)) {
            log.warn("VNPay callback bị từ chối: chữ ký không hợp lệ. IP={}", request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .status(400).message("Chữ ký không hợp lệ").data(null).build());
        }

        String responseCode = request.getParameter("vnp_ResponseCode");
        String orderInfo = request.getParameter("vnp_OrderInfo");

        String[] parts = orderInfo.split("-", 2);
        if (parts.length < 2) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .status(400).message("OrderInfo không đúng định dạng").data(null).build());
        }

        long entityId;
        try {
            entityId = Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .status(400).message("ID trong OrderInfo không hợp lệ").data(null).build());
        }

        boolean paymentSuccess = "00".equals(responseCode);

        PaymentType paymentType;
        try {
            paymentType = PaymentType.valueOf(parts[1]);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .status(400).message("Loại thanh toán không hợp lệ: " + parts[1]).build());
        }

        if (paymentType == PaymentType.RENTAL_TOOL) {
            return handleRentalToolCallback(entityId, paymentSuccess);
        } else {
            return handlePendingBookingCallback(entityId, paymentSuccess);
        }
    }

    private ResponseEntity<ApiResponse<Map<String, Object>>> handleRentalToolCallback(long rentalToolId, boolean success) {
        RentalTool rentalTool = rentalToolRepository.findById(rentalToolId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn thuê id=" + rentalToolId));

        if (!success) {
            log.info("Thanh toán RENTAL_TOOL id={} thất bại", rentalToolId);
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .status(200).message("Thanh toán thất bại")
                    .data(Map.of("type", "RENTAL_TOOL", "status", "FAILED")).build());
        }

        rentalTool.setStatus(RentalToolStatus.IN_USE);
        rentalToolService.handleDailyRental(rentalTool);

        log.info("Thanh toán RENTAL_TOOL id={} thành công", rentalToolId);
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thanh toán vợt thuê thành công")
                .data(Map.of(
                        "type", "RENTAL_TOOL",
                        "rentalToolId", rentalToolId,
                        "rentalCode", rentalTool.getRentalToolCode()
                )).build());
    }

    private ResponseEntity<ApiResponse<Map<String, Object>>> handlePendingBookingCallback(long pendingId, boolean success) {
        PendingBookingData data = pendingBookingCache.get(pendingId)
                .orElse(null);

        if (data == null) {
            log.warn("Không tìm thấy phiên đặt sân trong cache: pendingId={}", pendingId);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .status(400).message("Phiên đặt sân không tồn tại hoặc đã hết hạn").data(null).build());
        }

        if (!success) {
            bookingService.cancelPendingBooking(data);
            pendingBookingCache.remove(pendingId);
            log.info("Thanh toán PENDING_BOOKING pendingId={} thất bại, đã giải phóng giữ chỗ", pendingId);
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .status(200).message("Thanh toán thất bại, vui lòng thử lại")
                    .data(Map.of("type", "BOOKING", "status", "FAILED")).build());
        }

        try {
            BookingResponseDTO booking = bookingService.confirmPendingBooking(data);
            pendingBookingCache.remove(pendingId);
            log.info("Thanh toán PENDING_BOOKING pendingId={} thành công, bookingId={}", pendingId, booking.getId());
            try {
                emailService.sendBookingConfirmationEmail(
                        data.getUser().getEmail(), booking.getBookingCode(), booking.getId());
            } catch (Exception e) {
                log.warn("Gửi email xác nhận thất bại cho {}: {}", data.getUser().getEmail(), e.getMessage());
            }
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .status(200).message("Thanh toán đặt sân thành công")
                    .data(Map.of("type", "BOOKING", "bookingId", booking.getId(), "bookingCode", booking.getBookingCode()))
                    .build());
        } catch (IllegalStateException e) {
            // Slot was taken by another user during payment — rare race condition
            pendingBookingCache.remove(pendingId);
            log.error("Xung đột lịch sau thanh toán thành công (pendingId={}): {}", pendingId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .status(409).message(e.getMessage()).data(null).build());
        }
    }
}
