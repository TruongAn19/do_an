package com.pitchbooking.app.service;

import com.pitchbooking.app.domain.Equipment;
import com.pitchbooking.app.domain.RentalType;
import org.springframework.stereotype.Service;

@Service
public class RentalPricingService {

    public double unitPrice(RentalType type, Equipment equipment) {
        return type == RentalType.DAILY
                ? equipment.getRentalPricePerDay()
                : equipment.getRentalPricePerPlay();
    }

    public double totalPrice(RentalType type, Equipment equipment, int quantity, Integer quantityDay) {
        if (type == RentalType.DAILY) {
            int days = (quantityDay != null) ? quantityDay : 1;
            return equipment.getRentalPricePerDay() * quantity * days;
        } else {
            return equipment.getRentalPricePerPlay() * quantity;
        }
    }
}
