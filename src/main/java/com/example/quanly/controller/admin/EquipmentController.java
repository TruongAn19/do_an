package com.example.quanly.controller.admin;

import com.example.quanly.domain.Equipment;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.service.EquipmentService;
import com.example.quanly.service.EquipmentStockByDateService;
import com.example.quanly.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final UploadService uploadService;
    private final EquipmentStockByDateService equipmentStockByDateService;

    @GetMapping("/api/v1/admin/equipments")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEquipments(
            @RequestParam(value = "page", defaultValue = "1") int page) {

        Pageable pageable = PageRequest.of(page - 1, 4);
        Page<Equipment> byProducts = equipmentService.getAllEquipment(pageable);

        Map<String, Object> result = Map.of(
                "equipments", byProducts.getContent(),
                "currentPage", page,
                "totalPages", byProducts.getTotalPages()
        );

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thành công").data(result).build());
    }

    @GetMapping("/api/v1/admin/equipments/{equipmentId}")
    public ResponseEntity<ApiResponse<Equipment>> getEquipmentDetail(@PathVariable long equipmentId) {
        Equipment equipment = equipmentService.getEquipmentById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị id=" + equipmentId));
        return ResponseEntity.ok(ApiResponse.<Equipment>builder()
                .status(200).message("Thành công").data(equipment).build());
    }

    @PostMapping(value = "/api/v1/admin/equipments", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Equipment>> createEquipment(
            @RequestPart("equipment") Equipment equipment,
            @RequestPart(value = "equipmentImg", required = false) MultipartFile file) {

        if (file != null && !file.isEmpty()) {
            equipment.setImage(uploadService.handleSaveUploadFile(file, "equipment"));
        }
        equipment.setStatus("ACTIVE");
        Equipment saved = equipmentService.handSaveEquipment(equipment);
        equipmentStockByDateService.generateStockForEquipment(saved);

        return ResponseEntity.ok(ApiResponse.<Equipment>builder()
                .status(200).message("Tạo thiết bị thành công").data(saved).build());
    }

    @PutMapping(value = "/api/v1/admin/equipments/{equipmentId}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Equipment>> updateEquipment(
            @PathVariable long equipmentId,
            @RequestPart("equipment") Equipment equipment,
            @RequestPart(value = "equipmentImg", required = false) MultipartFile file) {

        Equipment existing = equipmentService.getEquipmentById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị id=" + equipmentId));

        existing.setName(equipment.getName());
        existing.setPrice(equipment.getPrice());
        existing.setFactory(equipment.getFactory());
        existing.setAvailable(equipment.isAvailable());
        existing.setRentalPricePerDay(equipment.getRentalPricePerDay());
        existing.setRentalPricePerPlay(equipment.getRentalPricePerPlay());
        existing.setBookingStockQuantity(equipment.getBookingStockQuantity());
        existing.setQuantity(equipment.getQuantity());
        existing.setStatus(equipment.getStatus());
        existing.setProduct(equipment.getProduct());
        if (file != null && !file.isEmpty()) {
            existing.setImage(uploadService.handleSaveUploadFile(file, "equipment"));
        }
        equipmentService.handSaveEquipment(existing);

        return ResponseEntity.ok(ApiResponse.<Equipment>builder()
                .status(200).message("Cập nhật thiết bị thành công").data(existing).build());
    }
}
