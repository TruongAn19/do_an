package com.pitchbooking.app.service.pricing;

import com.pitchbooking.app.domain.AvailableTime;
import com.pitchbooking.app.domain.User;
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
