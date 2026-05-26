package com.pitchbooking.app.domain.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CheckStockRequest {
    private Long equipmentId;
    private LocalDate date;
}
