package com.pitchbooking.app.domain;

import lombok.Getter;

@Getter
public enum RentalPaymentStatus {
    UNPAID("Chưa thanh toán"),
    PAID("Đã thanh toán"),
    REFUNDED("Đã hoàn tiền");

    private final String label;

    RentalPaymentStatus(String label) {
        this.label = label;
    }

}
