package com.pitchbooking.app.controller.admin;

import com.pitchbooking.app.domain.dto.CheckStockRequest;
import com.pitchbooking.app.domain.dto.EquipmentStockAvailabilityResponse;
import com.pitchbooking.app.service.EquipmentStockByDateService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/equipment-stock")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EquipmentStockByDateController {

    EquipmentStockByDateService equipmentStockService;

    @PostMapping("")
    public ResponseEntity<EquipmentStockAvailabilityResponse> checkStock(
            @Valid @RequestBody CheckStockRequest request) {
        EquipmentStockAvailabilityResponse stock = equipmentStockService.getStock(request);
        return ResponseEntity.ok(stock);
    }
}
