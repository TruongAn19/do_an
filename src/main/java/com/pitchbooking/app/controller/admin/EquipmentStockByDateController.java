package com.pitchbooking.app.controller.admin;

import com.pitchbooking.app.domain.EquipmentStockByDate;
import com.pitchbooking.app.domain.dto.CheckStockRequest;
import com.pitchbooking.app.service.EquipmentStockByDateService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/equipment-stock")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EquipmentStockByDateController {

    EquipmentStockByDateService equipmentStockService;

    @PostMapping("")
    public ResponseEntity<EquipmentStockByDate> checkStock(@RequestBody CheckStockRequest request) {
        log.info("--------+++++=");
        EquipmentStockByDate stock = equipmentStockService.getStock(request);
        return ResponseEntity.ok(stock);
    }
}
