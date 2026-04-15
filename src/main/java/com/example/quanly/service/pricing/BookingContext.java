package com.example.quanly.service.pricing;

import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class BookingContext {
    private User user;
    private AvailableTime time;
    private LocalDate bookingDate;
}
