package com.pitchbooking.app.service.ai;

import com.pitchbooking.app.domain.AvailableTime;
import com.pitchbooking.app.domain.BookingDetail;
import com.pitchbooking.app.domain.PitchType;
import com.pitchbooking.app.domain.Product;
import com.pitchbooking.app.domain.SubPitch;
import com.pitchbooking.app.domain.TemporaryBooking;
import com.pitchbooking.app.repository.BookingDetailRepository;
import com.pitchbooking.app.repository.SubPitchAvailableTimeRepository;
import com.pitchbooking.app.repository.SubPitchRepository;
import com.pitchbooking.app.repository.TemporaryBookingRepository;
import com.pitchbooking.app.service.BookingStatsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIToolsConfigTest {

    @Mock SubPitchRepository subPitchRepository;
    @Mock SubPitchAvailableTimeRepository subPitchAvailableTimeRepository;
    @Mock BookingDetailRepository bookingDetailRepository;
    @Mock TemporaryBookingRepository temporaryBookingRepository;
    @Mock BookingStatsService bookingStatsService;

    @InjectMocks AIToolsConfig tools;

    @Test
    void listAllPitchesReturnsOnlyActiveProductsAndBusinessIdentifiers() {
        SubPitch active = pitch(11L, "Sân mini 1", product(1L, "Cụm Quận 1", "ACTIVE"));
        SubPitch inactive = pitch(22L, "Sân mini 2", product(2L, "Cụm đóng cửa", "INACTIVE"));
        when(subPitchRepository.findAll()).thenReturn(List.of(inactive, active));

        AIToolsConfig.AllPitchesResponse response = tools.listAllPitches();

        assertThat(response.pitches()).hasSize(1);
        assertThat(response.pitches().get(0))
                .extracting(
                        AIToolsConfig.PitchInfo::productId,
                        AIToolsConfig.PitchInfo::subPitchId,
                        AIToolsConfig.PitchInfo::pitchType)
                .containsExactly(1L, 11L, "FIVE_ASIDE");
    }

    @Test
    void availabilityUsesConfiguredSlotsAndExcludesBookingsAndActiveHolds() {
        LocalDate date = LocalDate.now().plusDays(1);
        SubPitch pitch = pitch(11L, "Sân mini 1", product(1L, "Cụm Quận 1", "ACTIVE"));
        AvailableTime bookedTime = time(1L, 18);
        AvailableTime heldTime = time(2L, 19);
        AvailableTime freeTime = time(3L, 20);

        BookingDetail booking = new BookingDetail();
        booking.setAvailableTime(bookedTime);
        TemporaryBooking activeHold = hold(heldTime, date, LocalDateTime.now().plusMinutes(5));
        TemporaryBooking expiredHold = hold(freeTime, date, LocalDateTime.now().minusMinutes(1));

        when(subPitchRepository.findAll()).thenReturn(List.of(pitch));
        when(bookingDetailRepository.findBySubPitchAndDate(pitch, date)).thenReturn(List.of(booking));
        when(temporaryBookingRepository.findBySubPitchAndBookingDate(pitch, date))
                .thenReturn(List.of(activeHold, expiredHold));
        when(subPitchAvailableTimeRepository.findAvailableTimesBySubPitch(pitch))
                .thenReturn(List.of(freeTime, heldTime, bookedTime));

        AIToolsConfig.PitchAvailabilityResponse response = tools.checkPitchAvailability(
                new AIToolsConfig.PitchAvailabilityRequest(date.toString(), 1L, 11L));

        assertThat(response.pitches()).hasSize(1);
        assertThat(response.pitches().get(0).availableTimes()).containsExactly("20:00");
    }

    @Test
    void availabilityRejectsPastDatesBeforeQueryingDatabase() {
        String pastDate = LocalDate.now().minusDays(1).toString();

        AIToolsConfig.PitchAvailabilityResponse response = tools.checkPitchAvailability(
                new AIToolsConfig.PitchAvailabilityRequest(pastDate, null, null));

        assertThat(response.pitches()).isEmpty();
        assertThat(response.message()).contains("ngày đã qua");
        verify(subPitchRepository, never()).findAll();
    }

    @Test
    void revenueReportNeverQueriesRevenueForNonAdmin() {
        AIToolsConfig.RevenueRequest request = new AIToolsConfig.RevenueRequest(
                "2026-08-01", "2026-08-31");

        AIToolsConfig.RevenueResponse response = tools.getRevenueReport(request, false);

        assertThat(response.authorized()).isFalse();
        assertThat(response.message()).contains("quản trị viên");
        verify(bookingStatsService, never()).getRevenueBetweenDates(
                LocalDate.parse(request.startDate()), LocalDate.parse(request.endDate()));
    }

    @Test
    void revenueReportCalculatesTotalForAdmin() {
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 2);
        Map<String, Double> daily = new LinkedHashMap<>();
        daily.put("2026-08-01", 200_000D);
        daily.put("2026-08-02", 350_000D);
        when(bookingStatsService.getRevenueBetweenDates(start, end)).thenReturn(daily);

        AIToolsConfig.RevenueResponse response = tools.getRevenueReport(
                new AIToolsConfig.RevenueRequest(start.toString(), end.toString()), true);

        assertThat(response.authorized()).isTrue();
        assertThat(response.totalRevenue()).isEqualTo(550_000D);
        assertThat(response.dailyRevenue()).containsExactlyEntriesOf(daily);
    }

    private Product product(long id, String name, String status) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setStatus(status);
        product.setAddress("TP.HCM");
        product.setAddressDetail("Quận 1");
        product.setPrice(200_000D);
        product.setSale(10L);
        return product;
    }

    private SubPitch pitch(long id, String name, Product product) {
        SubPitch pitch = new SubPitch();
        pitch.setId(id);
        pitch.setName(name);
        pitch.setPitchType(PitchType.FIVE_ASIDE);
        pitch.setProduct(product);
        return pitch;
    }

    private AvailableTime time(long id, int hour) {
        AvailableTime time = new AvailableTime();
        time.setId(id);
        time.setTime(LocalTime.of(hour, 0));
        return time;
    }

    private TemporaryBooking hold(
            AvailableTime time,
            LocalDate date,
            LocalDateTime expiresAt) {
        TemporaryBooking hold = new TemporaryBooking();
        hold.setAvailableTime(time);
        hold.setBookingDate(date);
        hold.setHoldExpiresAt(expiresAt);
        return hold;
    }
}
