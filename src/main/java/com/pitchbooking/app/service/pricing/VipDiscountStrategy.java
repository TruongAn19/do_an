package com.pitchbooking.app.service.pricing;

import org.springframework.stereotype.Component;

@Component
public class VipDiscountStrategy implements PricingStrategy {
    @Override
    public double calculatePrice(double basePrice, BookingContext context) {
        String level = context.getUser().getMemberLevel();
        if ("GOLD".equalsIgnoreCase(level)) {
            return basePrice * 0.8; // -20%
        } else if ("SILVER".equalsIgnoreCase(level)) {
            return basePrice * 0.9; // -10%
        }
        return basePrice;
    }
}
