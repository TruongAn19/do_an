package com.example.quanly.domain.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CheckStockRequest {
    private Long racketId;
    private LocalDate date;
}
