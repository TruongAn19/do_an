package com.example.quanly.domain.dto;

import lombok.Data;

@Data
public class CancelBookingRequest {
    /** Lý do huỷ (tuỳ chọn). */
    private String reason;
}
