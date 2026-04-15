package com.example.quanly.domain.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MatchPostResponseDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String area;
    private String playDateStr;
    private LocalDate playDate;
    private String timeSlot;
    private String skillLevel;
    private int maxParticipants;
    private int currentParticipants;
    private String status;
    private String description;
    private boolean owner;
    private boolean joined;
}
