package com.example.quanly.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotEvent {

    public enum Type { SLOT_HELD, SLOT_RELEASED }

    private Type type;
    private Long subCourtId;
    private Long availableTimeId;
    private LocalDate bookingDate;
    private Long userId;
    private LocalDateTime holdEndTime;
}
