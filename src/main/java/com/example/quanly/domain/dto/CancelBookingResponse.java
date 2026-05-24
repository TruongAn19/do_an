package com.example.quanly.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelBookingResponse {
    private long bookingId;
    private String bookingCode;
    private String status;             // DA_HUY
    private String refundStatus;       // PENDING_REFUND / NOT_APPLICABLE
    private double refundAmount;
    private Integer usedSessions;      // null cho ONE_TIME
    private Integer totalSessions;     // null cho ONE_TIME
    private String contactHotline;
    private String contactEmail;
    private String message;
}
