package com.pitchbooking.app.service.pricing;

import org.springframework.stereotype.Component;
import java.time.LocalTime;

@Component
public class PeakHourStrategy implements PricingStrategy {
    @Override
    public double calculatePrice(double basePrice, BookingContext context) {
        LocalTime time = context.getTime().getTime();
        boolean isWeekend = context.getBookingDate().getDayOfWeek().getValue() >= 6;
        boolean isPeakTime = time.isAfter(LocalTime.of(16, 59)) && time.isBefore(LocalTime.of(22, 1));

        if (isWeekend || isPeakTime) {
            return basePrice * 1.3; // +30%
        }
        return basePrice;
    }
}
