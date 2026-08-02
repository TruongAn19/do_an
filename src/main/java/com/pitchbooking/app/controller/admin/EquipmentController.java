package com.pitchbooking.app.controller.admin;

import com.pitchbooking.app.domain.Equipment;
import com.pitchbooking.app.domain.dto.ApiResponse;
import com.pitchbooking.app.domain.dto.EquipmentResponseDTO;
import com.pitchbooking.app.domain.dto.EquipmentUpsertRequest;
import com.pitchbooking.app.mapper.EquipmentResponseMapper;
import com.pitchbooking.app.service.EquipmentService;
import com.pitchbooking.app.service.EquipmentStockByDateService;
import com.pitchbooking.app.service.ProductService;
import com.pitchbooking.app.service.UploadService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
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
    private final ProductService productService;
    private final EquipmentResponseMapper equipmentResponseMapper;

    @GetMapping("/api/v1/admin/equipments")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEquipments(
            @RequestParam(value = "page", defaultValue = "1") int page) {

        Pageable pageable = PageRequest.of(page - 1, 4);
        Page<Equipment> byProducts = equipmentService.getAllEquipment(pageable);

        Map<String, Object> result = Map.of(
                "equipments", equipmentResponseMapper.toDTOs(byProducts.getContent()),
                "currentPage", page,
                "totalPages", byProducts.getTotalPages()
        );

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thành công").data(result).build());
    }

    @GetMapping("/api/v1/admin/equipments/{equipmentId}")
    public ResponseEntity<ApiResponse<EquipmentResponseDTO>> getEquipmentDetail(@PathVariable long equipmentId) {
        Equipment equipment = equipmentService.getEquipmentById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị id=" + equipmentId));
        return ResponseEntity.ok(ApiResponse.<EquipmentResponseDTO>builder()
                .status(200).message("Thành công").data(equipmentResponseMapper.toDTO(equipment)).build());
    }

    @PostMapping(value = "/api/v1/admin/equipments", consumes = "multipart/form-data")
    @Transactional
    public ResponseEntity<ApiResponse<EquipmentResponseDTO>> createEquipment(
            @Valid @RequestPart("equipment") EquipmentUpsertRequest request,
            @RequestPart(value = "equipmentImg", required = false) MultipartFile file) {

        validateStock(request);
        Equipment equipment = new Equipment();
        applyRequest(equipment, request);
        if (file != null && !file.isEmpty()) {
            equipment.setImage(uploadService.handleSaveUploadFile(file, "equipment"));
        } else if (request.getImage() != null && !request.getImage().isBlank()) {
            equipment.setImage(request.getImage().trim());
        }
        Equipment saved = equipmentService.handSaveEquipment(equipment);
        equipmentStockByDateService.generateStockForEquipment(saved);

        return ResponseEntity.ok(ApiResponse.<EquipmentResponseDTO>builder()
                .status(200).message("Tạo thiết bị thành công").data(equipmentResponseMapper.toDTO(saved)).build());
    }

    @PutMapping(value = "/api/v1/admin/equipments/{equipmentId}", consumes = "multipart/form-data")
    @Transactional
    public ResponseEntity<ApiResponse<EquipmentResponseDTO>> updateEquipment(
            @PathVariable long equipmentId,
            @Valid @RequestPart("equipment") EquipmentUpsertRequest request,
            @RequestPart(value = "equipmentImg", required = false) MultipartFile file) {

        validateStock(request);
        Equipment existing = equipmentService.getEquipmentById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị id=" + equipmentId));

        equipmentStockByDateService.updateFutureStockCapacity(
                existing.getId(), request.getQuantity());
        applyRequest(existing, request);
        if (file != null && !file.isEmpty()) {
            existing.setImage(uploadService.handleSaveUploadFile(file, "equipment"));
        } else if (request.getImage() != null && !request.getImage().isBlank()) {
            existing.setImage(request.getImage().trim());
        }
        Equipment saved = equipmentService.handSaveEquipment(existing);

        return ResponseEntity.ok(ApiResponse.<EquipmentResponseDTO>builder()
                .status(200).message("Cập nhật thiết bị thành công").data(equipmentResponseMapper.toDTO(saved)).build());
    }

    private void applyRequest(Equipment equipment, EquipmentUpsertRequest request) {
        equipment.setName(request.getName().trim());
        equipment.setFactory(request.getFactory());
        equipment.setPrice(request.getPrice());
        equipment.setAvailable(request.getAvailable());
        equipment.setRentalPricePerDay(request.getRentalPricePerDay());
        equipment.setRentalPricePerPlay(request.getRentalPricePerPlay());
        equipment.setBookingStockQuantity(request.getBookingStockQuantity());
        equipment.setQuantity(request.getQuantity());
        equipment.setStatus(request.getStatus());
        equipment.setProduct(productService.getRawProductById(request.getProductId()));
    }

    private void validateStock(EquipmentUpsertRequest request) {
        if (request.getBookingStockQuantity() > request.getQuantity()) {
            throw new IllegalArgumentException(
                    "Tồn kho cho thuê tại sân không được lớn hơn tổng tồn kho.");
        }
    }
}
