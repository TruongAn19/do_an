package com.pitchbooking.app.domain.dto;

import com.pitchbooking.app.domain.Notification;
import com.pitchbooking.app.domain.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private String refType;
    private Long refId;
    private boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationDTO from(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .refType(n.getRefType())
                .refId(n.getRefId())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
