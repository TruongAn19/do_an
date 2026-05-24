package com.example.quanly.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String type;       // REFUND_REQUEST / REFUND_DONE / BOOKING_CANCELLED / SYSTEM
    private String refType;
    private Long refId;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
}
