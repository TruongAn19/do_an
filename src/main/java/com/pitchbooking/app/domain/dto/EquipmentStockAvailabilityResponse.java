package com.pitchbooking.app.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EquipmentStockAvailabilityResponse {
    private Long equipmentId;
    private LocalDate date;
    private int availableStock;
    private int reservedStock;
    private int rentalStock;
    private int totalStock;
}
