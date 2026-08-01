package com.pitchbooking.app.service;

import com.pitchbooking.app.domain.AvailableTime;
import com.pitchbooking.app.domain.Booking;
import com.pitchbooking.app.domain.BookingDetail;
import com.pitchbooking.app.domain.BookingStatus;
import com.pitchbooking.app.domain.BookingType;
import com.pitchbooking.app.domain.Equipment;
import com.pitchbooking.app.domain.Product;
import com.pitchbooking.app.domain.RentalTool;
import com.pitchbooking.app.domain.RentalToolStatus;
import com.pitchbooking.app.domain.RentalType;
import com.pitchbooking.app.domain.SubPitch;
import com.pitchbooking.app.domain.SubPitchAvailableTime;
import com.pitchbooking.app.domain.TemporaryBooking;
import com.pitchbooking.app.domain.User;
import com.pitchbooking.app.domain.dto.BookingPriceBreakdown;
import com.pitchbooking.app.domain.dto.BookingResponseDTO;
import com.pitchbooking.app.domain.dto.PendingBookingData;
import com.pitchbooking.app.domain.dto.PreparedBookingResult;
import com.pitchbooking.app.config.ContactProperties;
import com.pitchbooking.app.exception.BusinessConflictException;
import com.pitchbooking.app.exception.ForbiddenOperationException;
import com.pitchbooking.app.mapper.BookingMapper;
import com.pitchbooking.app.repository.BookingDetailRepository;
import com.pitchbooking.app.repository.BookingRepository;
import com.pitchbooking.app.repository.EquipmentRepository;
import com.pitchbooking.app.repository.ProductRepository;
import com.pitchbooking.app.repository.RentalToolRepository;
import com.pitchbooking.app.repository.SubPitchRepository;
import com.pitchbooking.app.repository.SubPitchAvailableTimeRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    @Mock EquipmentRepository equipmentRepository;
    @Mock UserRepository userRepository;
    @Mock ProductRepository productRepository;
    @Mock TimeRepository timeRepository;
    @Mock SubPitchRepository subPitchRepository;
    @Mock SubPitchAvailableTimeRepository subPitchAvailableTimeRepository;
    @Mock TemporaryBookingRepository temporaryBookingRepository;
    @Mock BookingMapper bookingMapper;
    @Mock PricingService pricingService;
    @Mock PendingBookingCache pendingBookingCache;
    @Mock ContactProperties contactProperties;

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

        subPitch = new SubPitch();
        subPitch.setId(7L);
        subPitch.setProduct(product);

        availableTime = new AvailableTime();
        availableTime.setId(3L);
    }

    private void mockAvailableTimeForSubPitch() {
        SubPitchAvailableTime relation = new SubPitchAvailableTime();
        relation.setSubPitch(subPitch);
        relation.setAvailableTime(availableTime);
        when(subPitchAvailableTimeRepository.findBySubPitchAndAvailableTime(
                subPitch, availableTime)).thenReturn(Optional.of(relation));
    }

    @Test
    @DisplayName("Booking equipment list is derived from the booking parent pitch")
    void bookingEquipments_usesBookingProductInsteadOfClientProductId() {
        Booking booking = new Booking();
        booking.setId(77L);
        booking.setBookingCode("BK77");
        booking.setUser(user);

        BookingDetail detail = new BookingDetail();
        detail.setProduct(product);
        Equipment equipment = new Equipment();
        equipment.setId(5L);
        equipment.setProduct(product);

        when(bookingRepository.findByBookingCode("BK77")).thenReturn(booking);
        when(bookingDetailRepository.findByBookingId(booking.getId())).thenReturn(List.of(detail));
        when(equipmentRepository.findByProductAndAvailableTrue(product.getId()))
                .thenReturn(List.of(equipment));

        assertThat(bookingService.getAvailableEquipmentsForBooking("BK77", user.getId()))
                .containsExactly(equipment);
    }

    @Test
    @DisplayName("Booking detail includes equipment rentals linked to the booking")
    void fetchBookingById_includesLinkedEquipmentRentals() {
        Booking booking = new Booking();
        booking.setId(5L);

        Equipment equipment = new Equipment();
        equipment.setId(1L);
        equipment.setName("Vợt pickleball");

        RentalTool rental = new RentalTool();
        rental.setId(7L);
        rental.setRentalToolCode("RT1785587455035");
        rental.setBookingId("5");
        rental.setEquipmentId(1L);
        rental.setProductId(10L);
        rental.setQuantity(1);
        rental.setRentalPrice(20_000d);
        rental.setType(RentalType.ON_SITE);
        rental.setStatus(RentalToolStatus.PENDING);
        rental.setPaymentStatus(com.pitchbooking.app.domain.RentalPaymentStatus.PAID);

        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toDTO(booking)).thenReturn(new BookingResponseDTO());
        when(rentalToolRepository.findRentalToolsByBookingId("5")).thenReturn(List.of(rental));
        when(equipmentRepository.findAllById(any())).thenReturn(List.of(equipment));

        BookingResponseDTO result = bookingService.fetchBookingById(5L).orElseThrow();

        assertThat(result.getRentalTools()).hasSize(1);
        assertThat(result.getRentalTools().get(0).getEquipmentName()).isEqualTo("Vợt pickleball");
        assertThat(result.getRentalTools().get(0).getQuantity()).isEqualTo(1);
        assertThat(result.getRentalTools().get(0).getRentalPrice()).isEqualTo(20_000d);
        assertThat(result.getRentalTools().get(0).getPaymentStatus()).isEqualTo("PAID");
    }

    @Test
    @DisplayName("Booking equipment list cannot be read by another user")
    void bookingEquipments_otherUserIsRejected() {
        Booking booking = new Booking();
        booking.setId(77L);
        booking.setBookingCode("BK77");
        booking.setUser(user);
        when(bookingRepository.findByBookingCode("BK77")).thenReturn(booking);

        assertThatThrownBy(() -> bookingService.getAvailableEquipmentsForBooking("BK77", 99L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Booking không thuộc người dùng đang đăng nhập.");

        verify(equipmentRepository, never()).findByProductAndAvailableTrue(any());
    }

    @Test
    @DisplayName("Paying a booking completes bundled rentals and keeps them paid")
    void updateBooking_toPaid_completesBundledRentals() {
        Booking booking = new Booking();
        booking.setId(88L);
        booking.setStatus(BookingStatus.DA_DAT);

        Equipment equipment = new Equipment();
        equipment.setId(5L);
        equipment.setBookingStockQuantity(3);

        RentalTool rental = new RentalTool();
        rental.setId(99L);
        rental.setBookingId(String.valueOf(booking.getId()));
        rental.setType(RentalType.ON_SITE);
        rental.setStatus(RentalToolStatus.PENDING);
        rental.setPaymentStatus(com.pitchbooking.app.domain.RentalPaymentStatus.PAID);
        rental.setEquipmentId(equipment.getId());
        rental.setQuantity(2);
        rental.setOnSiteStockReserved(true);

        when(bookingRepository.findByIdWithLock(booking.getId())).thenReturn(Optional.of(booking));
        when(rentalToolRepository.findRentalToolsByBookingId(String.valueOf(booking.getId())))
                .thenReturn(List.of(rental));
        when(equipmentRepository.findByIdWithLock(equipment.getId()))
                .thenReturn(Optional.of(equipment));

        bookingService.updateBooking(booking.getId(), BookingStatus.DA_THANH_TOAN.name());

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.DA_THANH_TOAN);
        assertThat(rental.getStatus()).isEqualTo(RentalToolStatus.COMPLETED);
        assertThat(rental.getPaymentStatus())
                .isEqualTo(com.pitchbooking.app.domain.RentalPaymentStatus.PAID);
        assertThat(rental.isOnSiteStockReserved()).isFalse();
        assertThat(equipment.getBookingStockQuantity()).isEqualTo(5);
        verify(bookingRepository).save(booking);
        verify(equipmentRepository).save(equipment);
        verify(rentalToolRepository).save(rental);
    }

    @Test
    @DisplayName("Admin cannot bypass cancellation flow by updating booking status")
    void updateBooking_cancelledToPaid_returnsBusinessConflict() {
        Booking booking = new Booking();
        booking.setId(88L);
        booking.setStatus(BookingStatus.DA_HUY);

        when(bookingRepository.findByIdWithLock(booking.getId())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateBooking(
                booking.getId(), BookingStatus.DA_THANH_TOAN.name()))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("Không thể chuyển booking");

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.DA_HUY);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Cancelling booking releases only stock reserved by its ON_SITE rental")
    void cancelBooking_releasesReservedOnSiteStock() {
        Booking booking = new Booking();
        booking.setId(88L);
        booking.setStatus(BookingStatus.DA_DAT);
        booking.setBookingType(BookingType.ONE_TIME);
        booking.setBookingDate(LocalDate.now().plusDays(1));
        booking.setAvailableTime(availableTime);
        booking.setUser(user);
        booking.setDepositPrice(100_000d);

        availableTime.setTime(java.time.LocalTime.of(18, 0));

        Equipment equipment = new Equipment();
        equipment.setId(5L);
        equipment.setBookingStockQuantity(3);

        RentalTool rental = new RentalTool();
        rental.setId(99L);
        rental.setBookingId(String.valueOf(booking.getId()));
        rental.setType(RentalType.ON_SITE);
        rental.setStatus(RentalToolStatus.PENDING);
        rental.setEquipmentId(equipment.getId());
        rental.setQuantity(2);
        rental.setOnSiteStockReserved(true);
        rental.setPaymentStatus(com.pitchbooking.app.domain.RentalPaymentStatus.PAID);

        when(bookingRepository.findByIdWithLock(booking.getId()))
                .thenReturn(Optional.of(booking));
        when(rentalToolRepository.findRentalToolsByBookingId(String.valueOf(booking.getId())))
                .thenReturn(List.of(rental));
        when(equipmentRepository.findByIdWithLock(equipment.getId()))
                .thenReturn(Optional.of(equipment));

        bookingService.cancelByUser(booking.getId(), user.getId(), "Đổi lịch");

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.DA_HUY);
        assertThat(rental.getStatus()).isEqualTo(RentalToolStatus.CANCELLED);
        assertThat(rental.getPaymentStatus())
                .isEqualTo(com.pitchbooking.app.domain.RentalPaymentStatus.PAID);
        assertThat(rental.isOnSiteStockReserved()).isFalse();
        assertThat(equipment.getBookingStockQuantity()).isEqualTo(5);
        verify(equipmentRepository).save(equipment);
        verify(rentalToolRepository).save(rental);
    }

    @Test
    @DisplayName("Cancelling an already cancelled booking is a business conflict")
    void cancelBooking_alreadyCancelled_returnsBusinessConflict() {
        Booking booking = new Booking();
        booking.setId(88L);
        booking.setStatus(BookingStatus.DA_HUY);
        booking.setUser(user);

        when(bookingRepository.findByIdWithLock(booking.getId()))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelByUser(
                booking.getId(), user.getId(), "Hủy lại"))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessage("Đơn đặt sân này đã được hủy trước đó.");
    }

    @Test
    @DisplayName("Booking rejects a sub-pitch from another product")
    void prepareBooking_subPitchFromAnotherProduct_rejects() {
        Product anotherProduct = new Product();
        anotherProduct.setId(11L);
        subPitch.setProduct(anotherProduct);

        when(userRepository.findUserById(user.getId())).thenReturn(user);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(subPitchRepository.findByIdWithLock(subPitch.getId())).thenReturn(Optional.of(subPitch));
        when(timeRepository.findById(availableTime.getId())).thenReturn(Optional.of(availableTime));

        assertThatThrownBy(() -> bookingService.preparePendingBooking(
                user, "Receiver", "Address", "0900000000",
                product.getId(), availableTime.getId(), subPitch.getId(),
                LocalDate.now().plusDays(1), BookingType.ONE_TIME.name(),
                null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Sân phụ ID " + subPitch.getId()
                                + " không thuộc sân ID " + product.getId() + ".");

        verify(pricingService, never()).calculateBookingPriceBreakdown(
                any(), any(), any(), any(), any(), any(), any(), any());
        verify(pendingBookingCache, never()).store(any(PendingBookingData.class));
    }

    @Test
    @DisplayName("Booking rejects a time not configured for the sub-pitch")
    void prepareBooking_timeNotConfiguredForSubPitch_rejects() {
        when(userRepository.findUserById(user.getId())).thenReturn(user);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(subPitchRepository.findByIdWithLock(subPitch.getId())).thenReturn(Optional.of(subPitch));
        when(timeRepository.findById(availableTime.getId())).thenReturn(Optional.of(availableTime));
        when(subPitchAvailableTimeRepository.findBySubPitchAndAvailableTime(
                subPitch, availableTime)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.preparePendingBooking(
                user, "Receiver", "Address", "0900000000",
                product.getId(), availableTime.getId(), subPitch.getId(),
                LocalDate.now().plusDays(1), BookingType.ONE_TIME.name(),
                null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Khung giờ ID " + availableTime.getId()
                                + " không được cấu hình cho sân phụ ID "
                                + subPitch.getId() + ".");

        verify(pricingService, never()).calculateBookingPriceBreakdown(
                any(), any(), any(), any(), any(), any(), any(), any());
        verify(pendingBookingCache, never()).store(any(PendingBookingData.class));
    }

    @Test
    @DisplayName("WEEKLY_RECURRING holds every generated booking date")
    void prepareWeeklyBooking_holdsEveryGeneratedDate() {
        LocalDate anchorDate = LocalDate.now().plusDays(1);
        LocalDate secondDate = anchorDate.plusWeeks(1);
        LocalDate thirdDate = anchorDate.plusWeeks(2);
        LocalDate recurringEndDate = thirdDate;
        List<Integer> daysOfWeek = List.of(anchorDate.getDayOfWeek().getValue());
        List<PendingBookingData.SlotData> slots = List.of(
                new PendingBookingData.SlotData(anchorDate, 200_000d, 0L),
                new PendingBookingData.SlotData(secondDate, 200_000d, 0L),
                new PendingBookingData.SlotData(thirdDate, 200_000d, 0L));
        BookingPriceBreakdown breakdown = new BookingPriceBreakdown(
                slots, 600_000d, 300_000d, 0d, 0d);

        TemporaryBooking anchorHold = new TemporaryBooking();
        anchorHold.setId(101L);
        anchorHold.setUserId(user.getId());
        anchorHold.setSubPitch(subPitch);
        anchorHold.setAvailableTime(availableTime);
        anchorHold.setBookingDate(anchorDate);
        anchorHold.setHoldExpiresAt(LocalDateTime.now().plusMinutes(2));

        when(userRepository.findUserById(user.getId())).thenReturn(user);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(subPitchRepository.findByIdWithLock(subPitch.getId())).thenReturn(Optional.of(subPitch));
        when(timeRepository.findById(availableTime.getId())).thenReturn(Optional.of(availableTime));
        mockAvailableTimeForSubPitch();
        when(pricingService.calculateBookingPriceBreakdown(
                user, product, availableTime, BookingType.WEEKLY_RECURRING,
                anchorDate, recurringEndDate, daysOfWeek, null))
                .thenReturn(breakdown);
        when(bookingDetailRepository.findBySubPitchAndAvailableTimeAndDate(
                subPitch, availableTime, anchorDate)).thenReturn(Optional.empty());
        when(bookingDetailRepository.findBySubPitchAndAvailableTimeAndDate(
                subPitch, availableTime, secondDate)).thenReturn(Optional.empty());
        when(bookingDetailRepository.findBySubPitchAndAvailableTimeAndDate(
                subPitch, availableTime, thirdDate)).thenReturn(Optional.empty());
        when(temporaryBookingRepository.findBySubPitchAndAvailableTimeAndBookingDateWithLock(
                subPitch, availableTime, anchorDate)).thenReturn(Optional.of(anchorHold));
        when(temporaryBookingRepository.findBySubPitchAndAvailableTimeAndBookingDateWithLock(
                subPitch, availableTime, secondDate)).thenReturn(Optional.empty());
        when(temporaryBookingRepository.findBySubPitchAndAvailableTimeAndBookingDateWithLock(
                subPitch, availableTime, thirdDate)).thenReturn(Optional.empty());

        AtomicLong nextHoldId = new AtomicLong(200L);
        when(temporaryBookingRepository.save(any(TemporaryBooking.class))).thenAnswer(invocation -> {
            TemporaryBooking hold = invocation.getArgument(0);
            if (hold.getId() == null) {
                hold.setId(nextHoldId.incrementAndGet());
            }
            return hold;
        });
        when(pendingBookingCache.store(any(PendingBookingData.class))).thenReturn(1234L);

        PreparedBookingResult result = bookingService.preparePendingBooking(
                user, "Receiver", "Address", "0900000000",
                product.getId(), availableTime.getId(), subPitch.getId(),
                anchorDate, BookingType.WEEKLY_RECURRING.name(), recurringEndDate,
                daysOfWeek, null);

        ArgumentCaptor<PendingBookingData> pendingCaptor =
                ArgumentCaptor.forClass(PendingBookingData.class);
        verify(pendingBookingCache).store(pendingCaptor.capture());
        assertThat(pendingCaptor.getValue().getTemporaryBookingIds())
                .containsExactly(101L, 201L, 202L);
        assertThat(pendingCaptor.getValue().getSlots())
                .extracting(PendingBookingData.SlotData::getDate)
                .containsExactly(anchorDate, secondDate, thirdDate);
        assertThat(result.pendingId()).isEqualTo(1234L);
        assertThat(result.depositPrice()).isEqualTo(300_000d);
        verify(subPitchRepository).findByIdWithLock(subPitch.getId());
    }

    @Test
    @DisplayName("WEEKLY_RECURRING rejects a generated date held by another user")
    void prepareWeeklyBooking_dateHeldByAnotherUser_rejects() {
        LocalDate anchorDate = LocalDate.now().plusDays(1);
        LocalDate secondDate = anchorDate.plusWeeks(1);
        LocalDate recurringEndDate = secondDate;
        List<Integer> daysOfWeek = List.of(anchorDate.getDayOfWeek().getValue());
        BookingPriceBreakdown breakdown = new BookingPriceBreakdown(
                List.of(
                        new PendingBookingData.SlotData(anchorDate, 200_000d, 0L),
                        new PendingBookingData.SlotData(secondDate, 200_000d, 0L)),
                400_000d, 200_000d, 0d, 0d);

        TemporaryBooking anchorHold = new TemporaryBooking();
        anchorHold.setId(101L);
        anchorHold.setUserId(user.getId());
        anchorHold.setHoldExpiresAt(LocalDateTime.now().plusMinutes(2));

        TemporaryBooking otherUserHold = new TemporaryBooking();
        otherUserHold.setId(102L);
        otherUserHold.setUserId(99L);
        otherUserHold.setHoldExpiresAt(LocalDateTime.now().plusMinutes(2));

        when(userRepository.findUserById(user.getId())).thenReturn(user);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(subPitchRepository.findByIdWithLock(subPitch.getId())).thenReturn(Optional.of(subPitch));
        when(timeRepository.findById(availableTime.getId())).thenReturn(Optional.of(availableTime));
        mockAvailableTimeForSubPitch();
        when(pricingService.calculateBookingPriceBreakdown(
                user, product, availableTime, BookingType.WEEKLY_RECURRING,
                anchorDate, recurringEndDate, daysOfWeek, null))
                .thenReturn(breakdown);
        when(bookingDetailRepository.findBySubPitchAndAvailableTimeAndDate(
                subPitch, availableTime, anchorDate)).thenReturn(Optional.empty());
        when(bookingDetailRepository.findBySubPitchAndAvailableTimeAndDate(
                subPitch, availableTime, secondDate)).thenReturn(Optional.empty());
        when(temporaryBookingRepository.findBySubPitchAndAvailableTimeAndBookingDateWithLock(
                subPitch, availableTime, anchorDate)).thenReturn(Optional.of(anchorHold));
        when(temporaryBookingRepository.findBySubPitchAndAvailableTimeAndBookingDateWithLock(
                subPitch, availableTime, secondDate)).thenReturn(Optional.of(otherUserHold));
        when(temporaryBookingRepository.save(anchorHold)).thenReturn(anchorHold);

        assertThatThrownBy(() -> bookingService.preparePendingBooking(
                user, "Receiver", "Address", "0900000000",
                product.getId(), availableTime.getId(), subPitch.getId(),
                anchorDate, BookingType.WEEKLY_RECURRING.name(), recurringEndDate,
                daysOfWeek, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(secondDate.format(
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        verify(pendingBookingCache, never()).store(any(PendingBookingData.class));
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
                List.of(new PendingBookingData.SlotData(LocalDate.now().plusDays(1), 400_000d, 0L)),
                List.of(999L));

        when(userRepository.findUserById(user.getId())).thenReturn(user);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(subPitchRepository.findByIdWithLock(subPitch.getId())).thenReturn(Optional.of(subPitch));
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
        verify(temporaryBookingRepository).deleteAllById(List.of(999L));
        assertThat(result.getStatus()).isEqualTo("DA_DAT");
    }

    @Test
    @DisplayName("Paid pending booking creates bundled ON_SITE rentals and keeps pitch deposit separate")
    void confirmPendingBooking_withEquipments_createsPaidBundledRentals() {
        LocalDate bookingDate = LocalDate.now().plusDays(1);
        PendingBookingData data = new PendingBookingData(
                999L, user.getId(), user.getEmail(),
                "Receiver", "Address", "0900000000",
                product.getId(), availableTime.getId(), subPitch.getId(),
                bookingDate, BookingType.ONE_TIME,
                null, null, null,
                400_000d, 50_000d,
                List.of(new PendingBookingData.SlotData(bookingDate, 400_000d, 0L)),
                List.of(999L));
        data.setEquipmentRentalPrice(60_000d);

        Equipment equipment = new Equipment();
        equipment.setId(5L);
        equipment.setName("Ball");
        equipment.setAvailable(true);
        equipment.setProduct(product);
        equipment.setPrice(500_000d);
        equipment.setBookingStockQuantity(5);
        data.setEquipments(List.of(new PendingBookingData.EquipmentSelectionData(
                equipment.getId(), 2, 30_000d, 60_000d)));

        when(userRepository.findUserById(user.getId())).thenReturn(user);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(subPitchRepository.findByIdWithLock(subPitch.getId())).thenReturn(Optional.of(subPitch));
        when(timeRepository.findById(availableTime.getId())).thenReturn(Optional.of(availableTime));
        when(bookingDetailRepository.findBySubPitchAndAvailableTimeAndDate(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(equipmentRepository.findByIdWithLock(equipment.getId())).thenReturn(Optional.of(equipment));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(123L);
            return booking;
        });
        when(rentalToolRepository.save(any(RentalTool.class))).thenAnswer(invocation -> {
            RentalTool rental = invocation.getArgument(0);
            rental.setId(456L);
            rental.setRentalToolCode("RT456");
            return rental;
        });
        when(bookingMapper.toDTO(any(Booking.class))).thenReturn(new BookingResponseDTO());

        bookingService.confirmPendingBooking(data);

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository, times(2)).save(bookingCaptor.capture());
        Booking savedBooking = bookingCaptor.getAllValues().get(1);
        assertThat(savedBooking.getDepositPrice()).isEqualTo(50_000d);
        assertThat(savedBooking.getTotalPrice()).isEqualTo(460_000d);
        assertThat(savedBooking.getRentalToolCode()).isEqualTo("RT456");

        ArgumentCaptor<RentalTool> rentalCaptor = ArgumentCaptor.forClass(RentalTool.class);
        verify(rentalToolRepository).save(rentalCaptor.capture());
        RentalTool rental = rentalCaptor.getValue();
        assertThat(rental.getType()).isEqualTo(RentalType.ON_SITE);
        assertThat(rental.getPaymentStatus()).isEqualTo(com.pitchbooking.app.domain.RentalPaymentStatus.PAID);
        assertThat(rental.getRentalPrice()).isEqualTo(60_000d);
        assertThat(rental.getBookingId()).isEqualTo("123");
        assertThat(equipment.getBookingStockQuantity()).isEqualTo(3);
    }
}
