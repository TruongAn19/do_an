package com.example.quanly.service;

import com.example.quanly.domain.Racket;
import com.example.quanly.domain.RentalType;
import org.springframework.stereotype.Service;

@Service
public class RentalPricingService {

    public double unitPrice(RentalType type, Racket racket) {
        return type == RentalType.DAILY
                ? racket.getRentalPricePerDay()
                : racket.getRentalPricePerPlay();
    }

    public double totalPrice(RentalType type, Racket racket, int quantity, int quantityDay) {
        return type == RentalType.DAILY
                ? racket.getRentalPricePerDay() * quantity * quantityDay
                : racket.getRentalPricePerPlay() * quantity;
    }
}
