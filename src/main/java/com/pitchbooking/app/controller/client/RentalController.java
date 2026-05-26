package com.pitchbooking.app.controller.client;

import com.pitchbooking.app.domain.PaymentMethod;
import com.pitchbooking.app.domain.PaymentType;
import com.pitchbooking.app.domain.RentalTool;
import com.pitchbooking.app.domain.RentalType;
import com.pitchbooking.app.domain.RentalToolStatus;
import com.pitchbooking.app.domain.User;
import com.pitchbooking.app.domain.dto.ApiResponse;
import com.pitchbooking.app.domain.dto.CreateRentalRequest;
import com.pitchbooking.app.domain.dto.PaymentRequest;
import com.pitchbooking.app.domain.dto.RentalPaymentRequest;
import com.pitchbooking.app.domain.dto.RentalToolDTO;
import com.pitchbooking.app.domain.dto.VnpayResponse;
import com.pitchbooking.app.exception.ForbiddenOperationException;
import com.pitchbooking.app.exception.ResourceNotFoundException;
import com.pitchbooking.app.repository.RentalToolRepository;
import com.pitchbooking.app.service.PaymentService;
import com.pitchbooking.app.service.RentalToolService;
import com.pitchbooking.app.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/rentals")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RentalController {

    RentalToolService rentalToolService;
    RentalToolRepository rentalToolRepository;
    PaymentService paymentService;
    SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<RentalToolDTO>> createRental(
            @Valid @RequestBody CreateRentalRequest request) {

        User currentUser = securityUtils.getCurrentUser();
        RentalToolDTO saved = rentalToolService.handleSubmitRental(request, currentUser);

        String message = request.getType() == RentalType.ON_SITE
                ? "Thuê thiết bị tại sân thành công"
                : "Đơn thuê đã được tạo, vui lòng tiến hành thanh toán";

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<RentalToolDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message(message)
                        .data(saved)
                        .build());
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<Map<String, Object>>> payForRental(
            @PathVariable Long id,
            @Valid @RequestBody RentalPaymentRequest paymentReq,
            HttpServletRequest request) {

        RentalTool rentalTool = rentalToolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê id=" + id));

        User currentUser = securityUtils.getCurrentUser();
        if (!rentalTool.getUserId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("Bạn không có quyền truy cập đơn thuê này");
        }

        if (rentalTool.getStatus() != RentalToolStatus.PENDING) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .status(409).message("Đơn thuê không ở trạng thái chờ thanh toán").build());
        }

        if (paymentReq.getPaymentMethod() == PaymentMethod.VNPAY) {
            PaymentRequest payReq = PaymentRequest.builder()
                    .id(rentalTool.getId())
                    .amount(rentalTool.getRentalPrice())
                    .type(PaymentType.RENTAL_TOOL)
                    .redirectUrl("")
                    .build();
            VnpayResponse vnpayResponse = paymentService.createVnPayPayment(payReq, request);
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .status(200).message("Chuyển hướng thanh toán")
                    .data(Map.of("paymentUrl", vnpayResponse.getPaymentUrl())).build());
        }

        rentalTool.setStatus(RentalToolStatus.PAID);
        rentalToolService.handleDailyRental(rentalTool);
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thuê thiết bị thành công")
                .data(Map.of("rentalToolId", rentalTool.getId())).build());
    }
}
