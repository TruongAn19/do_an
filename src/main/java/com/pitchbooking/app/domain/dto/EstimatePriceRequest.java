package com.pitchbooking.app.domain.dto;

import com.pitchbooking.app.domain.BookingType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class EstimatePriceRequest {
    @Positive private long productId;
    @Positive private long availableTimeId;
    @NotNull @FutureOrPresent @JsonFormat(pattern = "yyyy-MM-dd") private LocalDate bookingDate;
    @NotNull private BookingType bookingType = BookingType.ONE_TIME;
    @FutureOrPresent @JsonFormat(pattern = "yyyy-MM-dd") private LocalDate recurringEndDate;
    private List<Integer> daysOfWeek;
    private Integer durationMonths;
}
