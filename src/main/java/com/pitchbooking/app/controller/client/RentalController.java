package com.pitchbooking.app.controller.client;

import com.pitchbooking.app.domain.NotificationType;
import com.pitchbooking.app.domain.PaymentMethod;
import com.pitchbooking.app.domain.PaymentType;
import com.pitchbooking.app.domain.RentalTool;
import com.pitchbooking.app.domain.RentalType;
import com.pitchbooking.app.domain.RentalToolStatus;
import com.pitchbooking.app.domain.User;
import com.pitchbooking.app.domain.dto.ApiResponse;
import com.pitchbooking.app.domain.dto.CreateRentalRequest;
import com.pitchbooking.app.domain.dto.NotificationDTO;
import com.pitchbooking.app.domain.dto.PaymentRequest;
import com.pitchbooking.app.domain.dto.RentalPaymentRequest;
import com.pitchbooking.app.domain.dto.RentalToolDTO;
import com.pitchbooking.app.domain.dto.VnpayResponse;
import com.pitchbooking.app.exception.ForbiddenOperationException;
import com.pitchbooking.app.exception.ResourceNotFoundException;
import com.pitchbooking.app.repository.RentalToolRepository;
import com.pitchbooking.app.service.NotificationService;
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

import java.util.List;
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
    NotificationService notificationService;

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

        rentalToolService.confirmCashPayment(id);
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thuê thiết bị thành công")
                .data(Map.of("rentalToolId", id)).build());
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cancelRental(@PathVariable Long id) {
        RentalTool rentalTool = rentalToolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê id=" + id));

        User currentUser = securityUtils.getCurrentUser();
        if (!rentalTool.getUserId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("Bạn không có quyền huỷ đơn thuê này");
        }

        if (rentalTool.getStatus() != RentalToolStatus.PENDING
                && rentalTool.getStatus() != RentalToolStatus.DEPOSITED) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .status(409).message("Chỉ có thể huỷ đơn chưa nhận thiết bị").build());
        }

        // Chụp status trước khi đổi sang CANCELLED để biết có cần hoàn cọc không.
        RentalToolStatus statusBeforeCancel = rentalTool.getStatus();
        rentalToolService.changeStatus(id, RentalToolStatus.CANCELLED);

        boolean needsRefund = statusBeforeCancel == RentalToolStatus.DEPOSITED
                || statusBeforeCancel == RentalToolStatus.PAID;
        double depositAmount = needsRefund ? rentalTool.getRentalPrice() : 0;

        // Thông báo cho user
        String userMsg = needsRefund
                ? String.format("Bạn đã huỷ đơn thuê %s thành công. "
                        + "Tiền cọc %,.0fđ đang chờ quản trị viên xử lý.",
                        rentalTool.getRentalToolCode(), depositAmount)
                : String.format("Bạn đã huỷ đơn thuê %s thành công.",
                        rentalTool.getRentalToolCode());
        NotificationDTO userNotif = notificationService.create(
                currentUser.getId(),
                NotificationType.BOOKING_CANCELLED,
                "RENTAL_TOOL", rentalTool.getId(),
                "Huỷ đơn thuê thành công",
                userMsg);
        notificationService.pushToUser(currentUser.getId(), userNotif);

        // Thông báo cho admin/staff — chỉ khi đã có tiền cọc cần hoàn
        if (needsRefund) {
            String staffMsg = String.format(
                    "User %s vừa huỷ đơn thuê #%s (id=%d) — cần hoàn %,.0fđ.",
                    currentUser.getEmail(), rentalTool.getRentalToolCode(), rentalTool.getId(), depositAmount);
            List<Long> staffIds = notificationService.staffAndAdminUserIds();
            NotificationDTO broadcastDto = null;
            for (Long staffId : staffIds) {
                NotificationDTO n = notificationService.create(
                        staffId, NotificationType.REFUND_REQUEST,
                        "RENTAL_TOOL", rentalTool.getId(),
                        "Yêu cầu hoàn cọc thuê thiết bị",
                        staffMsg);
                if (broadcastDto == null) broadcastDto = n;
            }
            if (broadcastDto != null) {
                notificationService.pushToStaff(broadcastDto);
            }
        }

        String responseMsg = needsRefund
                ? String.format("Huỷ đơn thuê thành công. Tiền cọc %,.0fđ đang chờ quản trị viên xử lý.", depositAmount)
                : "Huỷ đơn thuê thành công";
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message(responseMsg)
                .data(Map.of("rentalToolId", id, "depositAmount", depositAmount)).build());
    }
}
