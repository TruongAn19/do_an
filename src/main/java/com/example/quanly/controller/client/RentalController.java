package com.example.quanly.controller.client;

import com.example.quanly.domain.PaymentMethod;
import com.example.quanly.domain.PaymentType;
import com.example.quanly.domain.RentalTool;
import com.example.quanly.domain.RentalType;
import com.example.quanly.domain.RentalToolStatus;
import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.domain.dto.CreateRentalRequest;
import com.example.quanly.domain.dto.PaymentRequest;
import com.example.quanly.domain.dto.RentalPaymentRequest;
import com.example.quanly.domain.dto.RentalToolDTO;
import com.example.quanly.domain.dto.VnpayResponse;
import com.example.quanly.exception.ForbiddenOperationException;
import com.example.quanly.exception.ResourceNotFoundException;
import com.example.quanly.repository.RentalToolRepository;
import com.example.quanly.service.PaymentService;
import com.example.quanly.service.RentalToolService;
import com.example.quanly.util.SecurityUtils;
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
                ? "Thuê vợt tại sân thành công"
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
                    .amount(rentalTool.getPrice())
                    .type(PaymentType.RENTAL_TOOL)
                    .redirectUrl("")
                    .build();
            VnpayResponse vnpayResponse = paymentService.createVnPayPayment(payReq, request);
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .status(200).message("Chuyển hướng thanh toán")
                    .data(Map.of("paymentUrl", vnpayResponse.getPaymentUrl())).build());
        }

        rentalTool.setStatus(RentalToolStatus.IN_USE);
        rentalToolService.handleDailyRental(rentalTool);
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thuê vợt thành công")
                .data(Map.of("rentalToolId", rentalTool.getId())).build());
    }
}
