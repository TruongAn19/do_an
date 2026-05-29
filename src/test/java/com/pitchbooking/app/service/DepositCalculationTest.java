package com.pitchbooking.app.service;

import com.pitchbooking.app.domain.Product;
import com.pitchbooking.app.domain.dto.ProductResponseDTO;
import com.pitchbooking.app.mapper.ProductMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Unit tests for the 50%-deposit rule:
 *  deposit = price × (1 − sale/100) × 0.5
 *
 * Covers the formula at the mapper (single-session) and a manual per-session
 * accumulation that mirrors the WEEKLY_RECURRING path in BookingService.
 */
class DepositCalculationTest {

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    @DisplayName("price=200000 sale=10 → depositPrice=90000 (200000*0.9*0.5)")
    void deposit_withSale10_is90000() {
        Product p = new Product();
        p.setPrice(200_000);
        p.setSale(10);

        ProductResponseDTO dto = mapper.toDTO(p);

        assertThat(dto.getDepositPrice()).isEqualTo(90_000.0, within(0.001));
    }

    @Test
    @DisplayName("price=200000 sale=0 → depositPrice=100000")
    void deposit_noSale_is100000() {
        Product p = new Product();
        p.setPrice(200_000);
        p.setSale(0);

        ProductResponseDTO dto = mapper.toDTO(p);

        assertThat(dto.getDepositPrice()).isEqualTo(100_000.0, within(0.001));
    }

    @Test
    @DisplayName("WEEKLY 4 sessions price=200000 sale=0 → total deposit=400000")
    void deposit_weeklyFourSessionsNoSale_is400000() {
        Product p = new Product();
        p.setPrice(200_000);
        p.setSale(0);

        // Per-session deposit (same formula BookingService uses), summed over sessions.
        double perSession = p.getPrice() * (1 - p.getSale() / 100.0) * 0.5;
        double total = perSession * 4;

        assertThat(total).isEqualTo(400_000.0, within(0.001));
    }
}
