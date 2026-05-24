package com.example.quanly.domain.dto;

import com.example.quanly.domain.BookingType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Redis-serializable snapshot of {@link PendingBookingData}.
 * Holds only IDs + primitive fields so it survives JSON round-trip without
 * carrying Hibernate proxies. Cache layer rehydrates entities by ID on read.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingBookingSnapshot {
    private List<Long> temporaryBookingIds;
    private Long userId;
    private String receiverName;
    private String receiverAddress;
    private String receiverPhone;
    private Long productId;
    private Long availableTimeId;
    private Long subCourtId;
    private LocalDate firstBookingDate;
    private BookingType bookingType;
    private LocalDate recurringEndDate;
    private double totalBookingPrice;
    private double depositPrice;
    private List<SlotSnapshot> slots;
    private List<RentalSnapshot> rentals;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotSnapshot {
        private LocalDate date;
        private double price;
        private long sale;

        @JsonCreator
        public static SlotSnapshot of(
                @JsonProperty("date") LocalDate date,
                @JsonProperty("price") double price,
                @JsonProperty("sale") long sale) {
            return new SlotSnapshot(date, price, sale);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RentalSnapshot {
        private Long racketId;
        private int quantity;
        private double unitPrice;
        private double subtotal;
    }
}
