package com.pitchbooking.app.service.pricing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final List<PricingStrategy> strategies;

    public double calculateFinalPrice(double basePrice, BookingContext context) {
        double finalPrice = basePrice;

        for (PricingStrategy strategy : strategies) {
            finalPrice = strategy.calculatePrice(finalPrice, context);
        }

        return finalPrice;
    }

    public double calculateRecurringDiscountRate(Integer durationMonths) {
        if (durationMonths == null)
            return 0;
        if (durationMonths >= 3)
            return 0.10; // 10%
        if (durationMonths >= 2)
            return 0.08; // 8%
        if (durationMonths >= 1)
            return 0.05; // 5%
        return 0;
    }
}
