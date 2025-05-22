package com.example.quanly.domain.dto;

import com.example.quanly.domain.AvailableTime;
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
