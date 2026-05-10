package com.example.quanly.service;

import com.example.quanly.domain.Equipment;
import com.example.quanly.domain.RentalType;
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
