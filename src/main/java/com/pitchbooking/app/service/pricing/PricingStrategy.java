package com.pitchbooking.app.service.pricing;

public interface PricingStrategy {
    double calculatePrice(double basePrice, BookingContext context);
}
