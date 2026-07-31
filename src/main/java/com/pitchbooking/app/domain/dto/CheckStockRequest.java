package com.pitchbooking.app.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CheckStockRequest {
    @NotNull(message = "Thiếu mã thiết bị")
    private Long equipmentId;

    @NotNull(message = "Thiếu ngày kiểm tra tồn kho")
    private LocalDate date;
}
