package com.example.quanly.domain.dto;

public record PendingBookingClaim(
        PendingBookingData data,
        boolean completed,
        Long bookingId,
        String bookingCode) {
}
