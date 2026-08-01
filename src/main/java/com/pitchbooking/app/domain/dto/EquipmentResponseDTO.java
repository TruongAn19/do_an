package com.pitchbooking.app.domain.dto;

/** Stable API shape for equipment; intentionally excludes the JPA entity graph. */
public record EquipmentResponseDTO(
        Long id,
        String name,
        double price,
        boolean available,
        String factory,
        String image,
        double rentalPricePerDay,
        double rentalPricePerPlay,
        int bookingStockQuantity,
        int quantity,
        String status,
        ProductSummary product) {

    public record ProductSummary(long id, String name) {
    }
}
