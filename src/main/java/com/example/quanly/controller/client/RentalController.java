package com.example.quanly.controller.client;

import com.example.quanly.domain.*;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.domain.dto.PaymentRequest;
import com.example.quanly.domain.dto.VnpayResponse;
import com.example.quanly.repository.BookingRepository;
import com.example.quanly.repository.RentalToolRepository;
import com.example.quanly.service.PaymentService;
import com.example.quanly.service.RacketService;
import com.example.quanly.service.RentalToolService;
import com.example.quanly.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import java.util.Map;

@RestController
public class RentalController {

    private final RacketService racketService;
    private final BookingRepository bookingRepository;
    private final RentalToolRepository rentalToolRepository;
    private final PaymentService paymentService;
    private final RentalToolService rentalToolService;
    private final UserService userService;

    public RentalController(RacketService racketService,
            BookingRepository bookingRepository, RentalToolRepository rentalToolRepository,
            PaymentService paymentService, RentalToolService rentalToolService,
            UserService userService) {
        this.racketService = racketService;
        this.bookingRepository = bookingRepository;
        this.rentalToolRepository = rentalToolRepository;
        this.paymentService = paymentService;
        this.rentalToolService = rentalToolService;
        this.userService = userService;
    }

    @GetMapping("/api/v1/client/rentals/{racketId}/page")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRentalPage(
            @PathVariable Long racketId,
            @RequestParam(value = "type") String type,
            @RequestParam(value = "bookingCode", required = false) String bookingCode) {

        Racket racket = racketService.getRacketById(racketId).orElse(null);
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("racket", racket);
        data.put("typeOrder", type);
        if (bookingCode != null)
            data.put("bookingCode", bookingCode);

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thành công").data(data).build());
    }

    @PostMapping("/api/v1/client/rentals/checkout")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitRental(
            @RequestBody RentalTool rentalTool,
            @RequestParam(value = "bookingId", required = false) String bookingId,
            Principal principal,
            HttpServletRequest request) {

        if ("ON_SITE".equals(rentalTool.getType())) {
            Booking booking = bookingRepository.findByBookingCode(bookingId);
            rentalTool.setBookingId(booking.getBookingCode());
        }

        User currentUser = userService.getUserByEmail(principal.getName());

        rentalToolService.handleSubmitRental(rentalTool, null, currentUser, request);

        if ("DAILY".equals(rentalTool.getType())) {
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .status(200).message("Chuyển sang thanh toán")
                    .data(Map.of("nextStep", "checkout")).build());
        }
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thuê vợt thành công")
                .data(Map.of("nextStep", "success")).build());
    }

    @PostMapping("/api/v1/client/rentals/daily-checkout")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitDailyCheckout(
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam("racketName") String racketName,
            HttpServletRequest request) {

        HttpSession session = request.getSession();
        RentalTool rentalTool = (RentalTool) session.getAttribute("pendingRentalTool");
        if (rentalTool == null) {
            throw new RuntimeException("Không tìm thấy đơn thuê trong session");
        }

        if ("VNPAY".equals(paymentMethod)) {
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setId(0L);
            paymentRequest.setAmount(rentalTool.getPrice());
            paymentRequest.setType("RENTAL_TOOL");
            paymentRequest.setRedirectUrl("");
            VnpayResponse vnpayResponse = paymentService.createVnPayPayment(paymentRequest, request);
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .status(200).message("Chuyển hướng thanh toán")
                    .data(Map.of("paymentUrl", vnpayResponse.getPaymentUrl())).build());
        } else {
            rentalToolService.handleDailyRental(rentalTool);
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .status(200).message("Thuê vợt thành công")
                    .data(Map.of("rentalTool", rentalTool, "racketName", racketName)).build());
        }
    }

    @GetMapping("/api/v1/payments/vnpay-callback")
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleVnpayCallback(HttpServletRequest request) {
        String status = request.getParameter("vnp_ResponseCode");
        String orderInfo = request.getParameter("vnp_OrderInfo");
        String type = orderInfo.split("-")[1];

        if ("RENTAL_TOOL".equals(type)) {
            if (!"00".equals(status)) {
                throw new RuntimeException("Thanh toán thất bại");
            }
            HttpSession session = request.getSession();
            RentalTool rentalTool = (RentalTool) session.getAttribute("pendingRentalTool");
            if (rentalTool == null)
                throw new RuntimeException("Không tìm thấy đơn thuê trong session");
            rentalToolService.handleDailyRental(rentalTool);
            rentalTool.setStatus(RentalToolStatus.PAID);
            rentalToolRepository.save(rentalTool);
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .status(200).message("Thanh toán vợt thuê thành công")
                    .data(Map.of("type", "RENTAL_TOOL")).build());
        } else {
            String bookingId = orderInfo.split("-")[0];
            Booking booking = bookingRepository.findById(Long.parseLong(bookingId))
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));
            if ("00".equals(status)) {
                booking.setStatus("Đã đặt");
                bookingRepository.save(booking);
                return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                        .status(200).message("Thanh toán đặt sân thành công")
                        .data(Map.of("type", "BOOKING", "bookingId", bookingId)).build());
            } else {
                bookingRepository.delete(booking);
                throw new RuntimeException("Thanh toán đặt sân thất bại");
            }
        }
    }
}
