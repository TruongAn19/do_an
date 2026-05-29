package com.pitchbooking.app.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Shared price-breakdown result for both /estimate (price preview) and
 * preparePendingBooking (confirm flow). Centralises the slot list + total +
 * deposit + recurring-discount math so the two paths cannot drift.
 */
@Data
@AllArgsConstructor
public class BookingPriceBreakdown {
    private List<PendingBookingData.SlotData> slots;
    private double totalPrice;
    private double depositPrice;
    private double discountRate;
    private double savings;
}
