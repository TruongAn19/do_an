package com.example.quanly.domain.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingDetailResponseDTO {
    private long id;
    private double price;
    private long sale;
    private LocalDate date;
    private Long productId;
    private String productName;
    private Long availableTimeId;
    private LocalTime availableTime;
    private Long subCourtId;
    private String subCourtName;
}
