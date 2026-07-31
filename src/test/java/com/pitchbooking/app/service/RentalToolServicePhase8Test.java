package com.pitchbooking.app.service;

import com.pitchbooking.app.domain.AvailableTime;
import com.pitchbooking.app.domain.Booking;
import com.pitchbooking.app.domain.BookingDetail;
import com.pitchbooking.app.domain.BookingStatus;
import com.pitchbooking.app.domain.Equipment;
import com.pitchbooking.app.domain.EquipmentStockByDate;
import com.pitchbooking.app.domain.Product;
import com.pitchbooking.app.domain.RentalPaymentStatus;
import com.pitchbooking.app.domain.RefundStatus;
import com.pitchbooking.app.domain.RentalTool;
import com.pitchbooking.app.domain.RentalToolStatus;
import com.pitchbooking.app.domain.RentalType;
import com.pitchbooking.app.domain.User;
import com.pitchbooking.app.domain.dto.CreateRentalRequest;
import com.pitchbooking.app.domain.dto.RentalToolDTO;
import com.pitchbooking.app.exception.BusinessConflictException;
import com.pitchbooking.app.exception.ForbiddenOperationException;
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
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        equipment.setBookingStockQuantity(5);
    }

    private CreateRentalRequest buildOnSiteRequest() {
        CreateRentalRequest req = new CreateRentalRequest();
        req.setFullName("Renter");
        req.setEmail(user.getEmail());
        req.setPhone("0900000000");
        req.setType(RentalType.ON_SITE);
        req.setEquipmentId(equipment.getId());
        req.setQuantity(2);
        req.setBookingCode("BK123");
        return req;
    }

    private Booking buildOnSiteBooking(LocalDate bookingDate, LocalTime slotTime, BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(777L);
        booking.setBookingCode("BK123");
        booking.setBookingDate(bookingDate);
        booking.setStatus(status);
        booking.setTotalPrice(0d);
        booking.setUser(user);

        AvailableTime availableTime = new AvailableTime();
        availableTime.setId(3L);
        availableTime.setTime(slotTime);
        booking.setAvailableTime(availableTime);
        return booking;
    }

    @Test
    @DisplayName("T8.7: ON_SITE expired booking is rejected")
    void t8_7_onSiteWithExpiredBooking_rejects() {
        CreateRentalRequest req = buildOnSiteRequest();
        Booking expiredBooking = buildOnSiteBooking(LocalDate.now().minusDays(1), LocalTime.of(18, 0), BookingStatus.DA_DAT);

        when(equipmentRepository.findById(equipment.getId())).thenReturn(Optional.of(equipment));
        when(rentalPricingService.totalPrice(RentalType.ON_SITE, equipment, 2, 0)).thenReturn(80_000d);
        when(userRepository.findUserById(user.getId())).thenReturn(user);
        when(bookingRepository.findByBookingCodeWithLock("BK123"))
                .thenReturn(Optional.of(expiredBooking));

        assertThatThrownBy(() -> rentalToolService.handleSubmitRental(req, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith(
                        "Booking đã hết giờ chơi, không thể thuê thêm phụ kiện. Hết hạn lúc ");

        verify(rentalToolRepository, never()).save(any(RentalTool.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("T8.7b: ON_SITE booking owned by another user is rejected")
    void t8_7b_onSiteBookingOwnedByAnotherUser_rejects() {
        CreateRentalRequest req = buildOnSiteRequest();
        Booking booking = buildOnSiteBooking(
                LocalDate.now().plusDays(1), LocalTime.of(18, 0), BookingStatus.DA_DAT);
        User bookingOwner = new User();
        bookingOwner.setId(99L);
        booking.setUser(bookingOwner);

        when(equipmentRepository.findById(equipment.getId())).thenReturn(Optional.of(equipment));
        when(rentalPricingService.totalPrice(RentalType.ON_SITE, equipment, 2, 0)).thenReturn(80_000d);
        when(userRepository.findUserById(user.getId())).thenReturn(user);
        when(bookingRepository.findByBookingCodeWithLock("BK123"))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> rentalToolService.handleSubmitRental(req, user))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Booking không thuộc người dùng đang đăng nhập.");

        verify(rentalToolRepository, never()).save(any(RentalTool.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("T8.8: ON_SITE valid bookingCode saves rental and updates booking total")
    void t8_8_onSiteWithValidBookingCode_succeeds() {
        CreateRentalRequest req = buildOnSiteRequest();
        Booking booking = buildOnSiteBooking(LocalDate.now().plusDays(1), LocalTime.of(18, 0), BookingStatus.DA_DAT);

        BookingDetail bookingDetail = new BookingDetail();
        bookingDetail.setPrice(500_000d);

        when(equipmentRepository.findById(equipment.getId())).thenReturn(Optional.of(equipment));
        when(rentalPricingService.totalPrice(RentalType.ON_SITE, equipment, 2, 0)).thenReturn(80_000d);
        when(userRepository.findUserById(user.getId())).thenReturn(user);
        when(bookingRepository.findByBookingCodeWithLock("BK123"))
                .thenReturn(Optional.of(booking));
        when(equipmentRepository.findByIdWithLock(equipment.getId()))
                .thenReturn(Optional.of(equipment));
        when(bookingDetailRepository.findByBookingId(booking.getId()))
                .thenReturn(java.util.List.of(bookingDetail));
        when(rentalToolRepository.findRentalToolsByBookingId(String.valueOf(booking.getId())))
                .thenReturn(java.util.List.of());
        when(rentalToolRepository.save(any(RentalTool.class))).thenAnswer(inv -> {
            RentalTool rt = inv.getArgument(0);
            rt.setId(1L);
            return rt;
        });
        when(rentalToolMapper.toDTO(any(RentalTool.class))).thenReturn(new RentalToolDTO());

        rentalToolService.handleSubmitRental(req, user);

        ArgumentCaptor<RentalTool> rtCap = ArgumentCaptor.forClass(RentalTool.class);
        verify(rentalToolRepository, atLeastOnce()).save(rtCap.capture());
        RentalTool savedRt = rtCap.getValue();
        assertThat(savedRt.getBookingId()).isEqualTo(String.valueOf(booking.getId()));
        assertThat(savedRt.getType()).isEqualTo(RentalType.ON_SITE);
        assertThat(savedRt.getEquipmentId()).isEqualTo(equipment.getId());
        assertThat(savedRt.isOnSiteStockReserved()).isTrue();
        assertThat(equipment.getBookingStockQuantity()).isEqualTo(3);
        verify(equipmentRepository).save(equipment);

        ArgumentCaptor<Booking> bookingCap = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCap.capture());
        Booking savedBooking = bookingCap.getValue();
        assertThat(savedBooking.getTotalPrice()).isEqualTo(500_000d + 80_000d);
        assertThat(savedBooking.getRentalToolCode()).isEqualTo(savedRt.getRentalToolCode());
    }

    @Test
    @DisplayName("T8.8a: ON_SITE keeps previous rental prices in booking total")
    void t8_8a_onSiteWithPreviousRentals_keepsCompleteBookingTotal() {
        CreateRentalRequest req = buildOnSiteRequest();
        Booking booking = buildOnSiteBooking(
                LocalDate.now().plusDays(1), LocalTime.of(18, 0), BookingStatus.DA_DAT);

        BookingDetail bookingDetail = new BookingDetail();
        bookingDetail.setPrice(500_000d);

        RentalTool previousRental = new RentalTool();
        previousRental.setType(RentalType.ON_SITE);
        previousRental.setStatus(RentalToolStatus.RENTING);
        previousRental.setRentalPrice(40_000d);

        RentalTool cancelledRental = new RentalTool();
        cancelledRental.setType(RentalType.ON_SITE);
        cancelledRental.setStatus(RentalToolStatus.CANCELLED);
        cancelledRental.setRentalPrice(30_000d);

        when(equipmentRepository.findById(equipment.getId())).thenReturn(Optional.of(equipment));
        when(rentalPricingService.totalPrice(RentalType.ON_SITE, equipment, 2, 0))
                .thenReturn(80_000d);
        when(userRepository.findUserById(user.getId())).thenReturn(user);
        when(bookingRepository.findByBookingCodeWithLock("BK123"))
                .thenReturn(Optional.of(booking));
        when(equipmentRepository.findByIdWithLock(equipment.getId()))
                .thenReturn(Optional.of(equipment));
        when(bookingDetailRepository.findByBookingId(booking.getId()))
                .thenReturn(java.util.List.of(bookingDetail));
        when(rentalToolRepository.findRentalToolsByBookingId(String.valueOf(booking.getId())))
                .thenReturn(java.util.List.of(previousRental, cancelledRental));
        when(rentalToolRepository.save(any(RentalTool.class))).thenAnswer(invocation -> {
            RentalTool saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        when(rentalToolMapper.toDTO(any(RentalTool.class))).thenReturn(new RentalToolDTO());

        rentalToolService.handleSubmitRental(req, user);

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        assertThat(bookingCaptor.getValue().getTotalPrice())
                .isEqualTo(500_000d + 40_000d + 80_000d);
    }

    @Test
    @DisplayName("ON_SITE rental rejects quantity greater than locked booking stock")
    void onSiteRental_insufficientLockedStock_rejectsWithoutSaving() {
        CreateRentalRequest req = buildOnSiteRequest();
        Booking booking = buildOnSiteBooking(
                LocalDate.now().plusDays(1), LocalTime.of(18, 0), BookingStatus.DA_DAT);
        equipment.setBookingStockQuantity(1);

        when(equipmentRepository.findById(equipment.getId())).thenReturn(Optional.of(equipment));
        when(rentalPricingService.totalPrice(RentalType.ON_SITE, equipment, 2, 0))
                .thenReturn(80_000d);
        when(userRepository.findUserById(user.getId())).thenReturn(user);
        when(bookingRepository.findByBookingCodeWithLock("BK123"))
                .thenReturn(Optional.of(booking));
        when(equipmentRepository.findByIdWithLock(equipment.getId()))
                .thenReturn(Optional.of(equipment));

        assertThatThrownBy(() -> rentalToolService.handleSubmitRental(req, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Không đủ phụ kiện tại sân. Số lượng còn lại: 1");

        assertThat(equipment.getBookingStockQuantity()).isEqualTo(1);
        verify(equipmentRepository, never()).save(any(Equipment.class));
        verify(rentalToolRepository, never()).save(any(RentalTool.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Cancelling ON_SITE rental releases reserved booking stock once")
    void cancelOnSiteRental_releasesReservedStock() {
        RentalTool rental = buildReservedOnSiteRental();
        when(rentalToolRepository.findByIdWithLock(rental.getId())).thenReturn(Optional.of(rental));
        when(equipmentRepository.findByIdWithLock(equipment.getId()))
                .thenReturn(Optional.of(equipment));
        when(rentalToolRepository.save(rental)).thenReturn(rental);
        when(rentalToolMapper.toDTO(rental)).thenReturn(new RentalToolDTO());

        rentalToolService.changeStatus(rental.getId(), RentalToolStatus.CANCELLED);

        assertThat(rental.getStatus()).isEqualTo(RentalToolStatus.CANCELLED);
        assertThat(rental.isOnSiteStockReserved()).isFalse();
        assertThat(equipment.getBookingStockQuantity()).isEqualTo(7);
        verify(equipmentRepository).save(equipment);
    }

    @Test
    @DisplayName("Completing ON_SITE rental releases reserved booking stock")
    void completeOnSiteRental_releasesReservedStock() {
        RentalTool rental = buildReservedOnSiteRental();
        rental.setStatus(RentalToolStatus.RENTING);
        when(rentalToolRepository.findByIdWithLock(rental.getId()))
                .thenReturn(Optional.of(rental));
        when(equipmentRepository.findByIdWithLock(equipment.getId()))
                .thenReturn(Optional.of(equipment));

        rentalToolService.completeRental(rental.getId());

        assertThat(rental.getStatus()).isEqualTo(RentalToolStatus.COMPLETED);
        assertThat(rental.getPaymentStatus()).isEqualTo(RentalPaymentStatus.PAID);
        assertThat(rental.isOnSiteStockReserved()).isFalse();
        assertThat(equipment.getBookingStockQuantity()).isEqualTo(7);
        verify(equipmentRepository).save(equipment);
    }

    @Test
    @DisplayName("T8.8b: ON_SITE invalid booking status is rejected")
    void t8_8b_onSiteWithInvalidStatus_rejects() {
        CreateRentalRequest req = buildOnSiteRequest();
        Booking booking = buildOnSiteBooking(LocalDate.now().plusDays(1), LocalTime.of(18, 0), BookingStatus.CHO_THANH_TOAN);

        when(equipmentRepository.findById(equipment.getId())).thenReturn(Optional.of(equipment));
        when(rentalPricingService.totalPrice(RentalType.ON_SITE, equipment, 2, 0)).thenReturn(80_000d);
        when(userRepository.findUserById(user.getId())).thenReturn(user);
        when(bookingRepository.findByBookingCodeWithLock("BK123"))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> rentalToolService.handleSubmitRental(req, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Booking không ở trạng thái cho phép thuê phụ kiện: " + BookingStatus.CHO_THANH_TOAN);

        verify(rentalToolRepository, never()).save(any(RentalTool.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("T8.9: DAILY rental with sufficient stock saves pending")
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
        when(equipmentStockByDateRepository.findByEquipmentIdAndDateWithLock(equipment.getId(), req.getRentalDate()))
                .thenReturn(Optional.of(stockDay1));
        when(equipmentStockByDateRepository.findByEquipmentIdAndDateWithLock(
                equipment.getId(), req.getRentalDate().plusDays(1)))
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
        assertThat(saved.getPaymentStatus()).isEqualTo(RentalPaymentStatus.UNPAID);
        assertThat(saved.isDailyStockReserved()).isTrue();
        assertThat(saved.getRentalPrice()).isEqualTo(40_000d);
        assertThat(saved.getPrice()).isEqualTo(equipment.getPrice() * req.getQuantity());
        assertThat(stockDay1.getAvailableStock()).isEqualTo(9);
        assertThat(stockDay1.getReservedStock()).isEqualTo(1);
        assertThat(stockDay2.getAvailableStock()).isEqualTo(9);
        assertThat(stockDay2.getReservedStock()).isEqualTo(1);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("DAILY payment updates payment status without reserving stock twice")
    void dailyPayment_doesNotReserveStockAgain() {
        LocalDate rentalDate = LocalDate.now().plusDays(1);
        RentalTool rental = buildPendingDailyRental(rentalDate, 2, 2);

        when(rentalToolRepository.findByIdWithLock(rental.getId()))
                .thenReturn(Optional.of(rental));
        when(rentalToolRepository.save(rental)).thenReturn(rental);

        rentalToolService.confirmCashPayment(rental.getId());

        assertThat(rental.getStatus()).isEqualTo(RentalToolStatus.PENDING);
        assertThat(rental.getPaymentStatus()).isEqualTo(RentalPaymentStatus.PAID);
        verify(equipmentStockByDateRepository, never())
                .findByEquipmentIdAndDateWithLock(any(), any());
        verify(equipmentStockByDateRepository, never()).save(any(EquipmentStockByDate.class));
    }

    @Test
    @DisplayName("DAILY creation rejects insufficient locked stock")
    void dailyCreation_insufficientLockedStock_rejectsWithoutSaving() {
        LocalDate rentalDate = LocalDate.now().plusDays(1);
        CreateRentalRequest req = buildDailyRequest(rentalDate, 2, 1);
        EquipmentStockByDate stock = stockWithAvailableQuantity(1);

        when(equipmentRepository.findById(equipment.getId())).thenReturn(Optional.of(equipment));
        when(rentalPricingService.totalPrice(RentalType.DAILY, equipment, 2, 1))
                .thenReturn(40_000d);
        when(equipmentStockByDateRepository
                .findByEquipmentIdAndDateWithLock(equipment.getId(), rentalDate))
                .thenReturn(Optional.of(stock));

        assertThatThrownBy(() -> rentalToolService.handleSubmitRental(req, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Không đủ thiết bị vào ngày " + rentalDate);

        assertThat(stock.getAvailableStock()).isEqualTo(1);
        assertThat(stock.getReservedStock()).isZero();
        verify(equipmentStockByDateRepository, never()).save(any(EquipmentStockByDate.class));
        verify(rentalToolRepository, never()).save(any(RentalTool.class));
    }

    @Test
    @DisplayName("Starting DAILY rental moves reserved stock to rental stock")
    void startDailyRental_movesReservedStockToRentalStock() {
        LocalDate rentalDate = LocalDate.now().plusDays(1);
        RentalTool rental = buildPendingDailyRental(rentalDate, 2, 1);
        EquipmentStockByDate stock = stockWithAvailableQuantity(1);
        stock.setReservedStock(2);

        when(rentalToolRepository.findByIdWithLock(rental.getId()))
                .thenReturn(Optional.of(rental));
        when(equipmentStockByDateRepository
                .findByEquipmentIdAndDateWithLock(equipment.getId(), rentalDate))
                .thenReturn(Optional.of(stock));
        when(rentalToolRepository.save(rental)).thenReturn(rental);

        rentalToolService.changeStatus(rental.getId(), RentalToolStatus.RENTING);

        assertThat(rental.getStatus()).isEqualTo(RentalToolStatus.RENTING);
        assertThat(stock.getReservedStock()).isZero();
        assertThat(stock.getRentalStock()).isEqualTo(2);
        assertThat(stock.getAvailableStock()).isEqualTo(1);
    }

    @Test
    @DisplayName("Completing DAILY rental returns held stock and records cash payment")
    void completeDailyRental_returnsStockAndMarksPaid() {
        LocalDate rentalDate = LocalDate.now().plusDays(1);
        RentalTool rental = buildPendingDailyRental(rentalDate, 2, 1);
        rental.setStatus(RentalToolStatus.RENTING);
        EquipmentStockByDate stock = stockWithAvailableQuantity(1);
        stock.setRentalStock(2);

        when(rentalToolRepository.findByIdWithLock(rental.getId()))
                .thenReturn(Optional.of(rental));
        when(equipmentStockByDateRepository
                .findByEquipmentIdAndDateWithLock(equipment.getId(), rentalDate))
                .thenReturn(Optional.of(stock));

        rentalToolService.completeRental(rental.getId());

        assertThat(rental.getStatus()).isEqualTo(RentalToolStatus.COMPLETED);
        assertThat(rental.getPaymentStatus()).isEqualTo(RentalPaymentStatus.PAID);
        assertThat(rental.isDailyStockReserved()).isFalse();
        assertThat(stock.getAvailableStock()).isEqualTo(3);
        assertThat(stock.getRentalStock()).isZero();
    }

    @Test
    @DisplayName("Completing DAILY rental never inflates stock when rental bucket is missing")
    void completeDailyRental_missingHeldStock_rejectsWithoutInflatingAvailableStock() {
        LocalDate rentalDate = LocalDate.now().plusDays(1);
        RentalTool rental = buildPendingDailyRental(rentalDate, 2, 1);
        rental.setStatus(RentalToolStatus.RENTING);
        EquipmentStockByDate stock = stockWithAvailableQuantity(5);

        when(rentalToolRepository.findByIdWithLock(rental.getId()))
                .thenReturn(Optional.of(rental));
        when(equipmentStockByDateRepository
                .findByEquipmentIdAndDateWithLock(equipment.getId(), rentalDate))
                .thenReturn(Optional.of(stock));

        assertThatThrownBy(() -> rentalToolService.completeRental(rental.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Tồn kho không còn giữ đủ số lượng của đơn thuê vào ngày "
                        + rentalDate + ".");

        assertThat(rental.getStatus()).isEqualTo(RentalToolStatus.RENTING);
        assertThat(stock.getAvailableStock()).isEqualTo(5);
        verify(equipmentStockByDateRepository, never()).save(any(EquipmentStockByDate.class));
        verify(rentalToolRepository, never()).save(any(RentalTool.class));
    }

    @Test
    @DisplayName("Cancelling paid DAILY rental releases stock and requests refund")
    void cancelPaidDailyRental_releasesReservedStockAndRequestsRefund() {
        LocalDate rentalDate = LocalDate.now().plusDays(1);
        RentalTool rental = buildPendingDailyRental(rentalDate, 2, 1);
        rental.setPaymentStatus(RentalPaymentStatus.PAID);
        rental.setRentalPrice(80_000d);
        EquipmentStockByDate stock = stockWithAvailableQuantity(1);
        stock.setReservedStock(2);

        when(rentalToolRepository.findByIdWithLock(rental.getId()))
                .thenReturn(Optional.of(rental));
        when(equipmentStockByDateRepository
                .findByEquipmentIdAndDateWithLock(equipment.getId(), rentalDate))
                .thenReturn(Optional.of(stock));
        when(rentalToolRepository.save(rental)).thenReturn(rental);

        rentalToolService.changeStatus(rental.getId(), RentalToolStatus.CANCELLED);

        assertThat(rental.getStatus()).isEqualTo(RentalToolStatus.CANCELLED);
        assertThat(rental.getPaymentStatus()).isEqualTo(RentalPaymentStatus.PAID);
        assertThat(rental.getRefundStatus()).isEqualTo(RefundStatus.PENDING_REFUND);
        assertThat(rental.getDepositAmount()).isEqualTo(80_000d);
        assertThat(rental.isDailyStockReserved()).isFalse();
        assertThat(stock.getAvailableStock()).isEqualTo(3);
        assertThat(stock.getReservedStock()).isZero();
    }

    @Test
    @DisplayName("Pending rental cannot skip handover and complete directly")
    void pendingRental_cannotCompleteDirectly() {
        RentalTool rental = buildPendingDailyRental(LocalDate.now().plusDays(1), 2, 1);
        when(rentalToolRepository.findByIdWithLock(rental.getId()))
                .thenReturn(Optional.of(rental));

        assertThatThrownBy(() -> rentalToolService.changeStatus(
                rental.getId(), RentalToolStatus.COMPLETED))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessage("Không thể chuyển đơn thuê từ PENDING sang COMPLETED.");

        assertThat(rental.getStatus()).isEqualTo(RentalToolStatus.PENDING);
        verify(equipmentStockByDateRepository, never())
                .findByEquipmentIdAndDateWithLock(any(), any());
        verify(equipmentStockByDateRepository, never()).save(any(EquipmentStockByDate.class));
        verify(rentalToolRepository, never()).save(any(RentalTool.class));
    }

    @Test
    @DisplayName("Repeated successful VNPay callback only keeps payment status paid")
    void repeatedVnpayCallback_isIdempotent() {
        RentalTool rental = buildPendingDailyRental(LocalDate.now().plusDays(1), 2, 1);
        rental.setPaymentStatus(RentalPaymentStatus.PAID);
        when(rentalToolRepository.findByIdWithLock(rental.getId()))
                .thenReturn(Optional.of(rental));

        RentalTool result = rentalToolService.confirmVnpayPayment(rental.getId());

        assertThat(result).isSameAs(rental);
        verify(equipmentStockByDateRepository, never())
                .findByEquipmentIdAndDateWithLock(any(), any());
        verify(equipmentStockByDateRepository, never()).save(any(EquipmentStockByDate.class));
        verify(rentalToolRepository, never()).save(any(RentalTool.class));
    }

    @Test
    @DisplayName("Confirming a rental refund outside pending-refund is a business conflict")
    void confirmRentalRefund_invalidRefundStatus_returnsBusinessConflict() {
        RentalTool rental = new RentalTool();
        rental.setId(93L);
        rental.setRefundStatus(RefundStatus.NONE);
        when(rentalToolRepository.findByIdWithLock(rental.getId()))
                .thenReturn(Optional.of(rental));

        assertThatThrownBy(() -> rentalToolService.confirmRentalRefund(rental.getId()))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessage("Đơn thuê không ở trạng thái chờ hoàn cọc");

        verify(rentalToolRepository, never()).save(any(RentalTool.class));
    }

    private CreateRentalRequest buildDailyRequest(
            LocalDate rentalDate,
            int quantity,
            int quantityDay) {
        CreateRentalRequest req = new CreateRentalRequest();
        req.setFullName("Renter");
        req.setEmail(user.getEmail());
        req.setPhone("0900000000");
        req.setType(RentalType.DAILY);
        req.setEquipmentId(equipment.getId());
        req.setQuantity(quantity);
        req.setQuantityDay(quantityDay);
        req.setRentalDate(rentalDate);
        return req;
    }

    private RentalTool buildPendingDailyRental(
            LocalDate rentalDate,
            int quantity,
            int quantityDay) {
        RentalTool rental = new RentalTool();
        rental.setId(91L);
        rental.setType(RentalType.DAILY);
        rental.setStatus(RentalToolStatus.PENDING);
        rental.setEquipmentId(equipment.getId());
        rental.setRentalDate(rentalDate);
        rental.setQuantity(quantity);
        rental.setQuantityDay(quantityDay);
        rental.setDailyStockReserved(true);
        return rental;
    }

    private RentalTool buildReservedOnSiteRental() {
        RentalTool rental = new RentalTool();
        rental.setId(92L);
        rental.setType(RentalType.ON_SITE);
        rental.setStatus(RentalToolStatus.PENDING);
        rental.setEquipmentId(equipment.getId());
        rental.setQuantity(2);
        rental.setOnSiteStockReserved(true);
        return rental;
    }

    private EquipmentStockByDate stockWithAvailableQuantity(int availableStock) {
        EquipmentStockByDate stock = new EquipmentStockByDate();
        stock.setAvailableStock(availableStock);
        return stock;
    }
}
