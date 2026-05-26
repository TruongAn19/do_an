package com.pitchbooking.app.domain.dto;

import com.pitchbooking.app.domain.AvailableTime;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
public class AvailableTimeDTO {
    private Long id;
    private String time; // String dạng "HH:mm"
    private String status;

    public AvailableTimeDTO(AvailableTime at) {
        this.id = at.getId();
        this.time = at.getTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
