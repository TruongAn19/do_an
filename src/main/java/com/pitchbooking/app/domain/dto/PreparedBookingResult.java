package com.pitchbooking.app.domain.dto;

public record PreparedBookingResult(
        long pendingId,
        double depositPrice,
        double equipmentRentalPrice,
        double paymentAmount) {}
