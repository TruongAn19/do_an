package com.pitchbooking.app.controller.admin;

import com.pitchbooking.app.domain.Equipment;
import com.pitchbooking.app.domain.NotificationType;
import com.pitchbooking.app.domain.RefundStatus;
import com.pitchbooking.app.domain.RentalTool;
import com.pitchbooking.app.domain.dto.ApiResponse;
import com.pitchbooking.app.domain.dto.NotificationDTO;
import com.pitchbooking.app.domain.dto.RentalToolDTO;
import com.pitchbooking.app.exception.ResourceNotFoundException;
import com.pitchbooking.app.repository.RentalToolRepository;
import com.pitchbooking.app.service.EquipmentService;
import com.pitchbooking.app.service.NotificationService;
import com.pitchbooking.app.service.RentalToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/rentals")
@RequiredArgsConstructor
public class RentalToolController {

        private final RentalToolService rentalToolService;
        private final EquipmentService equipmentService;
        private final NotificationService notificationService;
        private final RentalToolRepository rentalToolRepository;

        @GetMapping
        public ResponseEntity<ApiResponse<Map<String, Object>>> getRentals(
                        @RequestParam(value = "search", required = false) String searchTerm,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "5") int size) {

                Page<RentalToolDTO> rentals = (searchTerm != null && !searchTerm.isEmpty())
                                ? rentalToolService.fetchRentalToolCode(searchTerm, page, size)
                                : rentalToolService.getRentalByTypeDAILY(page, size);

                Map<String, Object> result = Map.of(
                                "rentals", rentals.getContent(),
                                "currentPage", page,
                                "totalPages", rentals.getTotalPages(),
                                "totalElements", rentals.getTotalElements());

                return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                                .status(200).message("Thành công").data(result).build());
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<Map<String, Object>>> getRentalDetail(@PathVariable Long id) {
                RentalToolDTO rentalTool = rentalToolService.getRentalToolById(id);
                Equipment equipment = equipmentService.getEquipmentById(Long.parseLong(rentalTool.getEquipmentId()))
                                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị"));

                Map<String, Object> result = Map.of("rentalTool", rentalTool, "equipment", equipment);

                return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                                .status(200).message("Thành công").data(result).build());
        }

        @PutMapping("/{id}/status")
        public ResponseEntity<ApiResponse<RentalToolDTO>> updateStatus(
                        @PathVariable Long id,
                        @RequestBody Map<String, String> body) {

                RentalToolDTO updated = rentalToolService.changeStatus(id,
                                com.pitchbooking.app.domain.RentalToolStatus.fromLabel(body.get("status")));

                return ResponseEntity.ok(ApiResponse.<RentalToolDTO>builder()
                                .status(200).message("Cập nhật trạng thái thành công").data(updated).build());
        }

        @GetMapping("/refunds")
        public ResponseEntity<ApiResponse<Map<String, Object>>> getRefunds(
                        @RequestParam(value = "refundStatus", required = false) RefundStatus refundStatus,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "10") int size) {

                Page<RentalToolDTO> rentals = rentalToolService.getRentalsByRefundStatus(refundStatus, page, size);

                Map<String, Object> result = Map.of(
                                "rentals", rentals.getContent(),
                                "currentPage", page,
                                "totalPages", rentals.getTotalPages(),
                                "totalElements", rentals.getTotalElements());

                return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                                .status(200).message("Thành công").data(result).build());
        }

        @PostMapping("/{id}/confirm-refund")
        public ResponseEntity<ApiResponse<RentalToolDTO>> confirmRefund(@PathVariable Long id) {
                RentalToolDTO updated = rentalToolService.confirmRentalRefund(id);

                RentalTool rental = rentalToolRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê id=" + id));

                if (rental.getUserId() != null) {
                        NotificationDTO notif = notificationService.create(
                                        rental.getUserId(),
                                        NotificationType.REFUND_DONE,
                                        "RENTAL_TOOL", id,
                                        "Hoàn cọc thành công",
                                        String.format("Đơn thuê %s đã được hoàn %,.0fđ.",
                                                        rental.getRentalToolCode(), rental.getDepositAmount()));
                        notificationService.pushToUser(rental.getUserId(), notif);
                }

                return ResponseEntity.ok(ApiResponse.<RentalToolDTO>builder()
                                .status(200).message("Xác nhận hoàn cọc thành công").data(updated).build());
        }
}
