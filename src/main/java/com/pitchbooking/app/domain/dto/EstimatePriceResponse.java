package com.pitchbooking.app.domain.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class EstimatePriceResponse {
    double basePrice;
    int sessions;
    double totalPrice;
    double depositPrice;
    double savings;
    double discountRate;
    double remainingPrice;
}
