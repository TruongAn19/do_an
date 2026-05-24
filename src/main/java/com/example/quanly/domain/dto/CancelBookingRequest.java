package com.example.quanly.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancelBookingRequest {

    @Size(max = 255, message = "Lý do huỷ tối đa 255 ký tự")
    private String reason;
}
