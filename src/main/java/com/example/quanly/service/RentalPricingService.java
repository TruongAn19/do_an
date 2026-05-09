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

    public double totalPrice(RentalType type, Equipment equipment, int quantity, int quantityDay) {
        return type == RentalType.DAILY
                ? equipment.getRentalPricePerDay() * quantity * quantityDay
                : equipment.getRentalPricePerPlay() * quantity;
    }
}
