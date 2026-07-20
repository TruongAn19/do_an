package com.example.quanly.controller;

import com.example.quanly.domain.NotificationType;
import com.example.quanly.domain.PaymentType;
import com.example.quanly.domain.RentalTool;
import com.example.quanly.domain.RentalToolStatus;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.domain.dto.BookingResponseDTO;
import com.example.quanly.domain.dto.PendingBookingData;
import com.example.quanly.repository.RentalToolRepository;
import com.example.quanly.service.BookingService;
import com.example.quanly.service.EmailService;
import com.example.quanly.service.NotificationService;
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
import java.util.Optional;

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
    NotificationService notificationService;

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

        // Đọc vnp_Amount
        String vnpAmountStr = request.getParameter("vnp_Amount");
        long vnpAmount = 0;
        if (vnpAmountStr != null && !vnpAmountStr.isBlank()) {
            try {
                vnpAmount = Long.parseLong(vnpAmountStr);
            } catch (NumberFormatException e) {
                log.warn("Giá trị vnp_Amount không phải là số nguyên hợp lệ: vnp_Amount={}", vnpAmountStr);
            }
        }

        PaymentType paymentType;
        try {
            paymentType = PaymentType.valueOf(parts[1]);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .status(400).message("Loại thanh toán không hợp lệ: " + parts[1]).build());
        }

        if (paymentType == PaymentType.RENTAL_TOOL) {
            return handleRentalToolCallback(entityId, paymentSuccess, vnpAmount, vnpAmountStr);
        } else {
            return handlePendingBookingCallback(entityId, paymentSuccess, vnpAmount, vnpAmountStr);
        }
    }

    private ResponseEntity<ApiResponse<Map<String, Object>>> handleRentalToolCallback(long rentalToolId, boolean success, long vnpAmount, String vnpAmountStr) {
        RentalTool rentalTool = rentalToolRepository.findById(rentalToolId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn thuê id=" + rentalToolId));

        if (!success) {
            log.info("Thanh toán RENTAL_TOOL id={} thất bại", rentalToolId);
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .status(200).message("Thanh toán thất bại")
                    .data(Map.of("type", "RENTAL_TOOL", "status", "FAILED")).build());
        }

        // A4: Đối chiếu số tiền VNPay thực nhận
        long expectedAmount = (long) (rentalTool.getPrice() * 100);
        if (vnpAmountStr == null || vnpAmountStr.isBlank() || vnpAmount != expectedAmount) {
            log.warn("Số tiền thanh toán RENTAL_TOOL id={} không khớp: nhận được {} (vnp_Amount={}), mong đợi {} (price={})",
                    rentalToolId, vnpAmount, vnpAmountStr, expectedAmount, rentalTool.getPrice());

            // B1: route qua cancelRental cho nhất quán convention. Đơn ở đây đang PENDING (chưa trừ
            // stock) nên cancelRental chỉ đổi trạng thái, không hoàn kho.
            rentalToolService.cancelRental(rentalToolId);

            // Bắn notification cho admin/staff yêu cầu rà soát hoàn tiền thủ công
            try {
                String staffMsg = String.format(
                        "Đã THU TIỀN thuê vợt %,.0f VNĐ của user #%d nhưng lệch số tiền thực nhận (nhận được: %s VNĐ, mong đợi: %,.0f VNĐ). Cần hoàn tiền thủ công.",
                        rentalTool.getPrice(), rentalTool.getUserId(),
                        vnpAmountStr != null && !vnpAmountStr.isBlank() ? String.format("%,d", Long.parseLong(vnpAmountStr) / 100) : "0",
                        rentalTool.getPrice());
                notificationService.sendToAdminStaff(NotificationType.REFUND_REQUEST,
                        "RENTAL_TOOL", rentalToolId,
                        "Cần hoàn tiền thủ công do lệch số tiền", staffMsg);
            } catch (Exception ex) {
                log.warn("Gửi notification hoàn tiền thủ công thất bại cho rentalToolId={}: {}", rentalToolId, ex.getMessage());
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .status(400).message("Số tiền thanh toán không khớp").data(null).build());
        }

        rentalTool = rentalToolService.handleDailyRental(rentalToolId);

        log.info("Thanh toán RENTAL_TOOL id={} thành công", rentalToolId);
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thanh toán vợt thuê thành công")
                .data(Map.of(
                        "type", "RENTAL_TOOL",
                        "rentalToolId", rentalToolId,
                        "rentalCode", rentalTool.getRentalToolCode()
                )).build());
    }

    private ResponseEntity<ApiResponse<Map<String, Object>>> handlePendingBookingCallback(long pendingId, boolean success, long vnpAmount, String vnpAmountStr) {
        Optional<String> completedResult = pendingBookingCache.getResult(pendingId);
        if (completedResult.isPresent()) {
            return duplicateBookingResponse(completedResult.get());
        }
        if (!pendingBookingCache.tryAcquireProcessing(pendingId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .status(409).message("Phiên đặt sân đang được xử lý").data(null).build());
        }

        try {
        // getAndRemove atomic (GETDEL): chỉ callback đầu tiên "giành" được snapshot, các callback trùng
        // (VNPay gọi cả IPN lẫn redirect) nhận empty → không tạo booking lần hai.
        PendingBookingData data = pendingBookingCache.get(pendingId)
                .orElse(null);

        if (data == null) {
            // Snapshot đã bị consume hoặc phiên hết hạn. Dùng marker kết quả để phân biệt:
            //   (a) đã có booking từ lần callback trước  → 200 + bookingCode đã có
            //   (b) hết hạn/không tồn tại thật            → 400 như cũ
            Optional<String> existingCode = pendingBookingCache.getResult(pendingId);
            if (existingCode.isPresent()) {
                log.info("Callback trùng lặp pendingId={}, đơn đã được xác nhận trước đó (bookingCode={})",
                        pendingId, existingCode.get());
                return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                        .status(200).message("Đơn đã được xác nhận trước đó")
                        .data(Map.of("type", "BOOKING", "bookingCode", existingCode.get())).build());
            }
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

        // A4: Đối chiếu số tiền VNPay thực nhận
        long expectedAmount = (long) (data.getDepositPrice() * 100);
        if (vnpAmountStr == null || vnpAmountStr.isBlank() || vnpAmount != expectedAmount) {
            log.warn("Số tiền thanh toán PENDING_BOOKING pendingId={} không khớp: nhận được {} (vnp_Amount={}), mong đợi {} (depositPrice={})",
                    pendingId, vnpAmount, vnpAmountStr, expectedAmount, data.getDepositPrice());

            bookingService.cancelPendingBooking(data);
            pendingBookingCache.remove(pendingId);

            // Bắn notification cho admin/staff yêu cầu rà soát hoàn tiền thủ công
            try {
                String staffMsg = String.format(
                        "Đã THU TIỀN cọc %,.0f VNĐ của user #%d nhưng lệch số tiền thực nhận (nhận được: %s VNĐ, mong đợi: %,.0f VNĐ). Cần hoàn tiền thủ công.",
                        data.getDepositPrice(), data.getUser().getId(),
                        vnpAmountStr != null && !vnpAmountStr.isBlank() ? String.format("%,d", Long.parseLong(vnpAmountStr) / 100) : "0",
                        data.getDepositPrice());
                notificationService.sendToAdminStaff(NotificationType.REFUND_REQUEST,
                        "PENDING_BOOKING", pendingId,
                        "Cần hoàn tiền thủ công do lệch số tiền", staffMsg);
            } catch (Exception ex) {
                log.warn("Gửi notification hoàn tiền thủ công thất bại cho pendingId={}: {}", pendingId, ex.getMessage());
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .status(400).message("Số tiền thanh toán không khớp").data(null).build());
        }

        try {
            BookingResponseDTO booking = bookingService.confirmPendingBooking(data, pendingId);
            // Ghi marker kết quả để callback trùng nhận diện được booking đã tạo.
            pendingBookingCache.storeResult(pendingId, booking.getBookingCode());
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
            pendingBookingCache.remove(pendingId);
            // Xung đột slot/hết tồn vợt (A1/A2) NHƯNG TIỀN ĐÃ ĐƯỢC THU. Snapshot đã bị getAndRemove
            // consume nên không thể retry → KHÔNG được im lặng nuốt mất tiền. Đánh dấu cần hoàn tiền
            // thủ công bằng notification cho admin/staff (tái dùng pattern REFUND_REQUEST của cancelByUser).
            log.error("Xung đột sau thanh toán THÀNH CÔNG (pendingId={}, userId={}, deposit={}): {} — CẦN HOÀN TIỀN THỦ CÔNG",
                    pendingId, data.getUser().getId(), data.getDepositPrice(), e.getMessage());
            try {
                String staffMsg = String.format(
                        "Đã THU TIỀN cọc %,.0f VNĐ của user #%d (SĐT %s) nhưng tạo đơn THẤT BẠI: %s. Cần hoàn tiền thủ công.",
                        data.getDepositPrice(), data.getUser().getId(),
                        data.getReceiverPhone() != null ? data.getReceiverPhone() : "(không có)",
                        e.getMessage());
                notificationService.sendToAdminStaff(NotificationType.REFUND_REQUEST,
                        "PENDING_BOOKING", pendingId,
                        "Cần hoàn tiền thủ công", staffMsg);
            } catch (Exception ex) {
                log.warn("Gửi notification hoàn tiền thủ công thất bại (pendingId={}): {}", pendingId, ex.getMessage());
            }
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .status(409).message(e.getMessage()).data(null).build());
        }
        } finally {
            pendingBookingCache.releaseProcessing(pendingId);
        }
    }

    private ResponseEntity<ApiResponse<Map<String, Object>>> duplicateBookingResponse(String bookingCode) {
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Đơn đã được xác nhận trước đó")
                .data(Map.of("type", "BOOKING", "bookingCode", bookingCode)).build());
    }
}
