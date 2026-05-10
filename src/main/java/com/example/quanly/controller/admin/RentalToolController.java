package com.example.quanly.controller.admin;

import com.example.quanly.domain.Equipment;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.domain.dto.RentalToolDTO;
import com.example.quanly.service.EquipmentService;
import com.example.quanly.service.RentalToolService;
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
                                com.example.quanly.domain.RentalToolStatus.fromLabel(body.get("status")));

                return ResponseEntity.ok(ApiResponse.<RentalToolDTO>builder()
                                .status(200).message("Cập nhật trạng thái thành công").data(updated).build());
        }
}
