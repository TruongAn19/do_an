package com.pitchbooking.app.domain.dto;

import com.pitchbooking.app.domain.BookingStatus;
import com.pitchbooking.app.domain.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CancelBookingResponse {
    private Long bookingId;
    private BookingStatus status;
    private RefundStatus refundStatus;
    private double refundAmount;
    private Integer usedSessions;
    private Integer totalSessions;
    private LocalDateTime cancelledAt;
    private String contactHotline;
    private String contactEmail;
}
