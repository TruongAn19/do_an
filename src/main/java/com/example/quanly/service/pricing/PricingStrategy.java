package com.example.quanly.service.pricing;

public interface PricingStrategy {
    double calculatePrice(double basePrice, BookingContext context);
}
