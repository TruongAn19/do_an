package com.example.quanly.domain.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MatchPostDTO {
    private LocalDate playDate;
    private String area;
    private String timeSlot;
    private String skillLevel;
    private String description;
}
