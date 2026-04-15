package com.example.quanly.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PlaceBookingRequest {
    private String receiverName;
    private String receiverAddress;
    private String receiverPhone;
    private long productId;
    private long availableTimeId;
    private long courtId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bookingDate;

    private String bookingType = "ONE_TIME"; // ONE_TIME, WEEKLY_RECURRING
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recurringEndDate;
}
