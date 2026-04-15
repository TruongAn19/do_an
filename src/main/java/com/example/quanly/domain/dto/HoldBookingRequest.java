package com.example.quanly.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HoldBookingRequest {
    private Long subCourtId;
    private Long availableTimeId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bookingDate;
}
