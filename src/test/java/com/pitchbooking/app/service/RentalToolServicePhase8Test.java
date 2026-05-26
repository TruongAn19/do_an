package com.pitchbooking.app.service;

import com.pitchbooking.app.domain.Booking;
import com.pitchbooking.app.domain.BookingDetail;
import com.pitchbooking.app.domain.Equipment;
import com.pitchbooking.app.domain.EquipmentStockByDate;
import com.pitchbooking.app.domain.Product;
import com.pitchbooking.app.domain.RentalTool;
import com.pitchbooking.app.domain.RentalToolStatus;
import com.pitchbooking.app.domain.RentalType;
import com.pitchbooking.app.domain.User;
import com.pitchbooking.app.domain.dto.CreateRentalRequest;
import com.pitchbooking.app.domain.dto.RentalToolDTO;
import com.pitchbooking.app.mapper.RentalToolMapper;
import com.pitchbooking.app.repository.BookingDetailRepository;
import com.pitchbooking.app.repository.BookingRepository;
import com.pitchbooking.app.repository.EquipmentRepository;
import com.pitchbooking.app.repository.EquipmentStockByDateRepository;
import com.pitchbooking.app.repository.RentalToolRepository;
import com.pitchbooking.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Phase 8 BE unit tests for the standalone rental flows that remain in place
 * alongside the (still-pending) bundled-rental refactor.
 *
 * <p>Covers:
 * <ul>
 *   <li><b>T8.8</b>: ON_SITE rental via bookingCode succeeds for an existing booking.</li>
 *   <li><b>T8.9</b>: DAILY standalone rental still works (status=PENDING saved when stock is ok).</li>
 * </ul>
 *
 * <p><b>T8.7 (bookingCode + expired booking → 400)</b> is blocked: the Phase 3
 * validation in {@code RentalToolService.handleOnSiteRental} (status check +
 * end-of-play-time check) is not yet implemented. The current implementation
 * accepts any existing booking regardless of status / time. Adding the test
 * here would assert behaviour the production code doesn't perform.
 */
@ExtendWith(MockitoExtension.class)
class RentalToolServicePhase8Test {

    @Mock RentalToolRepository rentalToolRepository;
    @Mock EquipmentRepository equipmentRepository;
    @Mock BookingRepository bookingRepository;
    @Mock EquipmentStockByDateRepository equipmentStockByDateRepository;
    @Mock UserRepository userRepository;
    @Mock BookingDetailRepository bookingDetailRepository;
    @Mock RentalToolMapper rentalToolMapper;
    @Mock RentalPricingService rentalPricingService;

    @InjectMocks RentalToolService rentalToolService;

    private User user;
    private Product product;
    private Equipment equipment;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(42L);
        user.setEmail("renter@test.com");

        product = new Product();
        product.setId(10L);

        equipment = new Equipment();
        equipment.setId(5L);
        equipment.setProduct(product);
        equipment.setPrice(1_200_000d);
    }

    /**
     * T8.8 — ON_SITE rental via a valid existing bookingCode is persisted with
     * the bookingId attached, and the booking totalPrice is updated to include
     * the rental price. This is the (still-supported) legacy flow.
     */
    @Test
    @DisplayName("T8.8: ON_SITE + valid bookingCode → RentalTool saved with bookingId, booking totalPrice updated")
    void t8_8_onSiteWithValidBookingCode_succeeds() {
        CreateRentalRequest req = new CreateRentalRequest();
        req.setFullName("Renter");
        req.setEmail(user.getEmail());
        req.setPhone("0900000000");
        req.setType(RentalType.ON_SITE);
        req.setEquipmentId(equipment.getId());
        req.setQuantity(2);
        req.setBookingCode("BK123");

        Booking booking = new Booking();
        booking.setId(777L);
        booking.setBookingCode("BK123");
        booking.setBookingDate(LocalDate.now().plusDays(1));
        booking.setTotalPrice(0d);

        BookingDetail bookingDetail = new BookingDetail();
        bookingDetail.setPrice(500_000d);

        when(equipmentRepository.findById(equipment.getId())).thenReturn(Optional.of(equipment));
        // CreateRentalRequest.quantityDay is a primitive int defaulting to 0 for ON_SITE.
        when(rentalPricingService.totalPrice(RentalType.ON_SITE, equipment, 2, 0)).thenReturn(80_000d);
        when(userRepository.findUserById(user.getId())).thenReturn(user);
        when(bookingRepository.findByBookingCode("BK123")).thenReturn(booking);
        when(bookingDetailRepository.findByBookingId(booking.getId()))
                .thenReturn(java.util.List.of(bookingDetail));
        when(rentalToolRepository.save(any(RentalTool.class))).thenAnswer(inv -> {
            RentalTool rt = inv.getArgument(0);
            rt.setId(1L);
            return rt;
        });
        when(rentalToolMapper.toDTO(any(RentalTool.class))).thenReturn(new RentalToolDTO());

        // The bundled-rental refactor hasn't injected the userId via JWT-aware
        // path; the existing flow takes the User from the controller. We feed
        // it in by setting it on the RentalTool before the service handles it.
        // handleSubmitRental(req, user) — user.getId() = 42L
        rentalToolService.handleSubmitRental(req, user);

        ArgumentCaptor<RentalTool> rtCap = ArgumentCaptor.forClass(RentalTool.class);
        verify(rentalToolRepository, atLeastOnce()).save(rtCap.capture());
        RentalTool savedRt = rtCap.getValue();
        assertThat(savedRt.getBookingId()).isEqualTo(String.valueOf(booking.getId()));
        assertThat(savedRt.getType()).isEqualTo(RentalType.ON_SITE);
        assertThat(savedRt.getEquipmentId()).isEqualTo(equipment.getId());

        // Booking total price should be updated = sum(bookingDetail.price) + rentalPrice
        ArgumentCaptor<Booking> bookingCap = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCap.capture());
        Booking savedBooking = bookingCap.getValue();
        assertThat(savedBooking.getTotalPrice()).isEqualTo(500_000d + 80_000d);
        assertThat(savedBooking.getRentalToolCode()).isEqualTo(savedRt.getRentalToolCode());
    }

    /**
     * T8.9 — Standalone DAILY rental keeps working: validates stock and persists
     * with status PENDING (next step is payment via {@code /pay} endpoint).
     */
    @Test
    @DisplayName("T8.9: DAILY rental with sufficient stock → status=PENDING saved")
    void t8_9_dailyRental_savesPending() {
        CreateRentalRequest req = new CreateRentalRequest();
        req.setFullName("Renter");
        req.setEmail(user.getEmail());
        req.setPhone("0900000000");
        req.setType(RentalType.DAILY);
        req.setEquipmentId(equipment.getId());
        req.setQuantity(1);
        req.setQuantityDay(2);
        req.setRentalDate(LocalDate.now().plusDays(1));

        EquipmentStockByDate stockDay1 = new EquipmentStockByDate();
        stockDay1.setAvailableStock(10);
        EquipmentStockByDate stockDay2 = new EquipmentStockByDate();
        stockDay2.setAvailableStock(10);

        when(equipmentRepository.findById(equipment.getId())).thenReturn(Optional.of(equipment));
        when(rentalPricingService.totalPrice(RentalType.DAILY, equipment, 1, 2)).thenReturn(40_000d);
        when(equipmentStockByDateRepository.findByEquipmentIdAndDate(equipment.getId(), req.getRentalDate()))
                .thenReturn(Optional.of(stockDay1));
        when(equipmentStockByDateRepository.findByEquipmentIdAndDate(equipment.getId(), req.getRentalDate().plusDays(1)))
                .thenReturn(Optional.of(stockDay2));
        when(rentalToolRepository.save(any(RentalTool.class))).thenAnswer(inv -> {
            RentalTool rt = inv.getArgument(0);
            rt.setId(2L);
            return rt;
        });
        when(rentalToolMapper.toDTO(any(RentalTool.class))).thenReturn(new RentalToolDTO());

        rentalToolService.handleSubmitRental(req, user);

        ArgumentCaptor<RentalTool> rtCap = ArgumentCaptor.forClass(RentalTool.class);
        verify(rentalToolRepository).save(rtCap.capture());
        RentalTool saved = rtCap.getValue();
        assertThat(saved.getType()).isEqualTo(RentalType.DAILY);
        assertThat(saved.getStatus()).isEqualTo(RentalToolStatus.PENDING);
        assertThat(saved.getRentalPrice()).isEqualTo(40_000d);
        assertThat(saved.getPrice()).isEqualTo(equipment.getPrice() * req.getQuantity());
        // Booking should NOT be touched on the DAILY path.
        verify(bookingRepository, org.mockito.Mockito.never()).save(any());
    }
}
