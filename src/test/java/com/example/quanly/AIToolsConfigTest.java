package com.example.quanly;

import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.Product;
import com.example.quanly.domain.SubCourt;
import com.example.quanly.domain.SubCourtAvailableTime;
import com.example.quanly.domain.TemporaryBooking;
import com.example.quanly.repository.BookingDetailRepository;
import com.example.quanly.repository.SubCourtAvailableTimeRepository;
import com.example.quanly.repository.SubCourtRepository;
import com.example.quanly.repository.TemporaryBookingRepository;
import com.example.quanly.service.BookingStatsService;
import com.example.quanly.service.ai.AIToolsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIToolsConfigTest {

    @Mock SubCourtRepository subCourtRepository;
    @Mock SubCourtAvailableTimeRepository subCourtAvailableTimeRepository;
    @Mock BookingDetailRepository bookingDetailRepository;
    @Mock TemporaryBookingRepository temporaryBookingRepository;
    @Mock BookingStatsService bookingStatsService;

    private AIToolsConfig tools;

    @BeforeEach
    void setUp() {
        tools = new AIToolsConfig(subCourtRepository, subCourtAvailableTimeRepository,
                bookingDetailRepository, temporaryBookingRepository, bookingStatsService);
    }

    @Test
    void availabilityOnlyReturnsTimesConfiguredForEachCourt() {
        LocalDate date = LocalDate.now().plusDays(1);
        SubCourt court = court(1L, "Sân 1", "Pickleball Center");
        AvailableTime configured = time(10L, 10, 0);
        AvailableTime notConfigured = time(11L, 11, 0);

        SubCourtAvailableTime relation = new SubCourtAvailableTime();
        relation.setSubCourt(court);
        relation.setAvailableTime(configured);

        when(subCourtRepository.findActiveCourts()).thenReturn(List.of(court));
        when(subCourtAvailableTimeRepository.findBySubCourt(court)).thenReturn(List.of(relation));
        when(bookingDetailRepository.findBySubCourtAndDate(court, date)).thenReturn(List.of());
        when(temporaryBookingRepository.findBySubCourtAndBookingDate(court, date)).thenReturn(List.of());

        AIToolsConfig.CourtAvailabilityResponse response = tools.checkCourtAvailability(
                new AIToolsConfig.CourtAvailabilityRequest(date.toString()));

        assertEquals(1, response.availableSlots().size());
        assertTrue(response.availableSlots().get(0).contains(configured.getTime().toString()));
        assertTrue(response.availableSlots().stream()
                .noneMatch(slot -> slot.contains(notConfigured.getTime().toString())));
    }

    @Test
    void unexpiredHoldIsNotReportedAsAvailable() {
        LocalDate date = LocalDate.now().plusDays(1);
        SubCourt court = court(1L, "Sân 1", "Pickleball Center");
        AvailableTime configured = time(10L, 10, 0);

        SubCourtAvailableTime relation = new SubCourtAvailableTime();
        relation.setSubCourt(court);
        relation.setAvailableTime(configured);

        TemporaryBooking hold = new TemporaryBooking();
        hold.setAvailableTime(configured);
        hold.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(subCourtRepository.findActiveCourts()).thenReturn(List.of(court));
        when(subCourtAvailableTimeRepository.findBySubCourt(court)).thenReturn(List.of(relation));
        when(bookingDetailRepository.findBySubCourtAndDate(court, date)).thenReturn(List.of());
        when(temporaryBookingRepository.findBySubCourtAndBookingDate(court, date)).thenReturn(List.of(hold));

        AIToolsConfig.CourtAvailabilityResponse response = tools.checkCourtAvailability(
                new AIToolsConfig.CourtAvailabilityRequest(date.toString()));

        assertEquals(List.of("Không còn sân trống nào trong ngày này."), response.availableSlots());
    }

    @Test
    void pastAvailabilityDateIsRejectedWithoutQueryingDatabase() {
        AIToolsConfig.CourtAvailabilityResponse response = tools.checkCourtAvailability(
                new AIToolsConfig.CourtAvailabilityRequest(LocalDate.now().minusDays(1).toString()));

        assertTrue(response.availableSlots().get(0).contains("quá khứ"));
        verify(subCourtRepository, never()).findActiveCourts();
    }

    @Test
    void revenueReportRequiresAdminAndValidDateRange() {
        AIToolsConfig.RevenueRequest request = new AIToolsConfig.RevenueRequest("2026-07-01", "2026-07-31");

        assertTrue(tools.getRevenueReport(request, false).report().contains("chỉ dành cho quản trị viên"));
        verify(bookingStatsService, never()).getRevenueBetweenDates(
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));

        AIToolsConfig.RevenueResponse invalidRange = tools.getRevenueReport(
                new AIToolsConfig.RevenueRequest("2026-07-31", "2026-07-01"), true);
        assertTrue(invalidRange.report().contains("không thể trước"));
    }

    @Test
    void adminRevenueReportUsesBackendStatistics() {
        LocalDate start = LocalDate.of(2026, 7, 1);
        LocalDate end = LocalDate.of(2026, 7, 31);
        when(bookingStatsService.getRevenueBetweenDates(start, end))
                .thenReturn(Map.of("Pickleball Center", 500_000D));

        AIToolsConfig.RevenueResponse response = tools.getRevenueReport(
                new AIToolsConfig.RevenueRequest(start.toString(), end.toString()), true);

        assertTrue(response.report().contains("Pickleball Center"));
        assertTrue(response.report().contains("500,000"));
    }

    private SubCourt court(Long id, String name, String productName) {
        Product product = new Product();
        product.setId(20L);
        product.setName(productName);
        product.setStatus("ACTIVE");

        SubCourt court = new SubCourt();
        court.setId(id);
        court.setName(name);
        court.setProduct(product);
        return court;
    }

    private AvailableTime time(Long id, int hour, int minute) {
        AvailableTime time = new AvailableTime();
        time.setId(id);
        time.setTime(LocalTime.of(hour, minute));
        return time;
    }
}
