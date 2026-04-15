package com.example.quanly.service.pricing;

import org.springframework.stereotype.Component;

@Component
public class DefaultPricingStrategy implements PricingStrategy {
    @Override
    public double calculatePrice(double basePrice, BookingContext context) {
        return basePrice;
    }
}
