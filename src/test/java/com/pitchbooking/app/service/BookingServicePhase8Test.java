package com.pitchbooking.app.service;

import com.pitchbooking.app.domain.AvailableTime;
import com.pitchbooking.app.domain.Booking;
import com.pitchbooking.app.domain.BookingStatus;
import com.pitchbooking.app.domain.BookingType;
import com.pitchbooking.app.domain.Product;
import com.pitchbooking.app.domain.SubPitch;
import com.pitchbooking.app.domain.User;
import com.pitchbooking.app.domain.dto.BookingResponseDTO;
import com.pitchbooking.app.domain.dto.PendingBookingData;
import com.pitchbooking.app.mapper.BookingMapper;
import com.pitchbooking.app.repository.BookingDetailRepository;
import com.pitchbooking.app.repository.BookingRepository;
import com.pitchbooking.app.repository.ProductRepository;
import com.pitchbooking.app.repository.RentalToolRepository;
import com.pitchbooking.app.repository.SubPitchRepository;
import com.pitchbooking.app.repository.TemporaryBookingRepository;
import com.pitchbooking.app.repository.TimeRepository;
import com.pitchbooking.app.repository.UserRepository;
import com.pitchbooking.app.service.pricing.PricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Phase 8 BE unit tests for the bundled-rental refactor (REFACTOR_RENTAL_TO_BOOKING.md).
 *
 * <p><b>Coverage in this file (cases that exercise behavior present in main):</b>
 * <ul>
 *   <li><b>T8.1</b>: ONE_TIME booking with no rackets → status DA_DAT, no RentalTool created.</li>
 * </ul>
 *
 * <p><b>Cases tracked but blocked on Phase 0–7 (bundled-rental feature not yet implemented):</b>
 * <ul>
 *   <li>T8.2 — ONE_TIME + 2 rackets → 2 RentalTools (needs PlaceBookingRequest.rackets + BookingService bundled-confirm logic).</li>
 *   <li>T8.3 — WEEKLY_RECURRING + rackets → 400 (same dependency).</li>
 *   <li>T8.4 — Racket out of stock → 400 (same dependency + Racket entity).</li>
 *   <li>T8.5 — Booking detail page response shape (FE work — BookingDetailComponent absent).</li>
 *   <li>T8.6 — Rental detail page (FE — RentalDetailComponent absent).</li>
 *   <li>T8.10 — Race condition on bundled racket stock (depends on bundled flow).</li>
 *   <li>T8.11 — @Transactional rollback when bundled rental insert fails (same).</li>
 *   <li>T8.12 — FE +/- button stock validation (depends on FE Phase 5 racket-picker).</li>
 * </ul>
 *
 * <p>T8.7/T8.8/T8.9 live in {@link RentalToolServicePhase8Test}.
 */
@ExtendWith(MockitoExtension.class)
class BookingServicePhase8Test {

    @Mock BookingRepository bookingRepository;
    @Mock BookingDetailRepository bookingDetailRepository;
    @Mock RentalToolRepository rentalToolRepository;
    @Mock UserRepository userRepository;
    @Mock ProductRepository productRepository;
    @Mock TimeRepository timeRepository;
    @Mock SubPitchRepository subPitchRepository;
    @Mock TemporaryBookingRepository temporaryBookingRepository;
    @Mock BookingMapper bookingMapper;
    @Mock PricingService pricingService;
    @Mock PendingBookingCache pendingBookingCache;

    @InjectMocks BookingService bookingService;

    private User user;
    private Product product;
    private SubPitch subPitch;
    private AvailableTime availableTime;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(42L);
        user.setEmail("buyer@test.com");

        product = new Product();
        product.setId(10L);
        product.setPrice(200_000);
        product.setSale(0);
        product.setDepositPrice(50_000);

        subPitch = new SubPitch();
        subPitch.setId(7L);

        availableTime = new AvailableTime();
        availableTime.setId(3L);
    }

    /**
     * T8.1 — Booking ONE_TIME with no rackets confirms to DA_DAT and creates no RentalTool.
     *
     * <p>The bundled-rental refactor (Phase 0–7) hasn't shipped yet, so this test
     * locks in the <em>current</em> behaviour: confirmPendingBooking never touches
     * rentalToolRepository. When Phase 0–7 lands and rentals are passed in, the
     * follow-up T8.2 test will assert the inverse for the same code path.
     */
    @Test
    @DisplayName("T8.1: ONE_TIME no rackets → status=DA_DAT, no RentalTool saved")
    void t8_1_oneTimeNoRackets_confirmsToDaDat_noRentalTool() {
        PendingBookingData data = new PendingBookingData(
                999L,                          // temporaryBookingId
                user.getId(), user.getEmail(),
                "Receiver", "Address", "0900000000",
                product.getId(), availableTime.getId(), subPitch.getId(),
                LocalDate.now().plusDays(1),
                BookingType.ONE_TIME,
                null, null, null,
                400_000d, 50_000d,
                List.of(new PendingBookingData.SlotData(LocalDate.now().plusDays(1), 400_000d, 0L)));

        when(userRepository.findUserById(user.getId())).thenReturn(user);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(subPitchRepository.findById(subPitch.getId())).thenReturn(Optional.of(subPitch));
        when(timeRepository.findById(availableTime.getId())).thenReturn(Optional.of(availableTime));
        when(bookingDetailRepository.findBySubPitchAndAvailableTimeAndDate(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(123L);
            return b;
        });
        when(bookingMapper.toDTO(any(Booking.class))).thenAnswer(inv -> {
            BookingResponseDTO dto = new BookingResponseDTO();
            dto.setId(((Booking) inv.getArgument(0)).getId());
            dto.setStatus("DA_DAT");
            return dto;
        });

        BookingResponseDTO result = bookingService.confirmPendingBooking(data);

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository, times(1)).save(bookingCaptor.capture());
        Booking saved = bookingCaptor.getValue();
        assertThat(saved.getStatus()).isEqualTo(BookingStatus.DA_DAT);
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());
        assertThat(saved.getAvailableTime().getId()).isEqualTo(availableTime.getId());

        verify(rentalToolRepository, never()).save(any());
        verify(rentalToolRepository, never()).saveAll(any());
        assertThat(result.getStatus()).isEqualTo("DA_DAT");
    }
}
