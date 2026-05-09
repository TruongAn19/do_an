package com.example.quanly.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HoldBookingRequest {

    @NotNull(message = "Sân phụ không được để trống")
    @Positive(message = "Sân phụ không hợp lệ")
    private Long subPitchId;

    @NotNull(message = "Khung giờ không được để trống")
    @Positive(message = "Khung giờ không hợp lệ")
    private Long availableTimeId;

    @NotNull(message = "Ngày đặt không được để trống")
    @FutureOrPresent(message = "Ngày đặt không thể ở quá khứ")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bookingDate;
}
