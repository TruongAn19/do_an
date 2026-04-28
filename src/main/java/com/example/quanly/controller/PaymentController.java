package com.example.quanly.controller;

import com.example.quanly.domain.Booking;
import com.example.quanly.domain.BookingStatus;
import com.example.quanly.domain.PaymentType;
import com.example.quanly.domain.RentalTool;
import com.example.quanly.domain.RentalToolStatus;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.repository.BookingRepository;
import com.example.quanly.repository.RentalToolRepository;
import com.example.quanly.service.PaymentService;
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

/**
 * Xử lý callback từ VNPay sau khi người dùng hoàn tất thanh toán.
 *
 * Điểm mấu chốt so với phiên bản cũ:
 *  1. Xác thực vnp_SecureHash trước khi làm bất kỳ điều gì → chống giả mạo callback.
 *  2. Tra cứu đơn hàng từ DB bằng ID trong OrderInfo thay vì từ HttpSession
 *     → hoàn toàn stateless, hoạt động đúng ngay cả khi server restart hoặc scale ngang.
 *  3. Tách biệt hoàn toàn khỏi RentalController → Single Responsibility.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PaymentController {

    PaymentService paymentService;
    RentalToolService rentalToolService;
    RentalToolRepository rentalToolRepository;
    BookingRepository bookingRepository;

    /**
     * GET /api/v1/payments/vnpay-callback
     *
     * VNPay gọi endpoint này sau khi người dùng thanh toán.
     * OrderInfo có định dạng: "{id}-{TYPE}" (vd: "42-RENTAL_TOOL", "7-BOOKING")
     */
    @GetMapping("/vnpay-callback")
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleVnpayCallback(HttpServletRequest request) {
        // Bước 1: Xác thực chữ ký — từ chối mọi request không hợp lệ
        if (!paymentService.verifyVnpayCallback(request)) {
            log.warn("VNPay callback bị từ chối: chữ ký không hợp lệ. IP={}", request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .status(400).message("Chữ ký không hợp lệ").data(null).build());
        }

        String responseCode = request.getParameter("vnp_ResponseCode");
        String orderInfo = request.getParameter("vnp_OrderInfo"); // e.g. "42-RENTAL_TOOL"

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
            return handleBookingCallback(entityId, paymentSuccess);
        }
    }

    private ResponseEntity<ApiResponse<Map<String, Object>>> handleRentalToolCallback(long rentalToolId, boolean success) {
        RentalTool rentalTool = rentalToolRepository.findById(rentalToolId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn thuê id=" + rentalToolId));

        if (!success) {
            log.info("Thanh toán RENTAL_TOOL id={} thất bại, giữ trạng thái PENDING", rentalToolId);
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .status(200).message("Thanh toán thất bại")
                    .data(Map.of("type", "RENTAL_TOOL", "status", "FAILED")).build());
        }

        // Đặt PAID trước rồi mới trừ tồn kho → handleDailyRental sẽ save với status PAID
        rentalTool.setStatus(RentalToolStatus.PAID);
        rentalToolService.handleDailyRental(rentalTool);

        log.info("Thanh toán RENTAL_TOOL id={} thành công", rentalToolId);
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thanh toán vợt thuê thành công")
                .data(Map.of("type", "RENTAL_TOOL", "rentalToolId", rentalToolId)).build());
    }

    private ResponseEntity<ApiResponse<Map<String, Object>>> handleBookingCallback(long bookingId, boolean success) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy booking id=" + bookingId));

        if (success) {
            booking.setStatus(BookingStatus.DA_THANH_TOAN);
            bookingRepository.save(booking);
            log.info("Thanh toán BOOKING id={} thành công", bookingId);
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .status(200).message("Thanh toán đặt sân thành công")
                    .data(Map.of("type", "BOOKING", "bookingId", bookingId)).build());
        } else {
            bookingRepository.delete(booking);
            log.info("Thanh toán BOOKING id={} thất bại, đã xóa booking", bookingId);
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .status(200).message("Thanh toán thất bại, đơn đặt sân đã bị hủy")
                    .data(Map.of("type", "BOOKING", "status", "FAILED")).build());
        }
    }
}
