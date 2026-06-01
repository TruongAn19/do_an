package com.example.quanly.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CancelBookingResponse {
    private Double refundAmount;
    private String refundStatus;
    private Integer usedSessions;
    private Integer totalSessions;
    private String hotline;
    private String email;
}
