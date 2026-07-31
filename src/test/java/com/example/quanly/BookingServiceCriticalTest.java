package com.example.quanly;

import com.example.quanly.config.ContactInfo;
import com.example.quanly.domain.Booking;
import com.example.quanly.domain.BookingStatus;
import com.example.quanly.domain.Racket;
import com.example.quanly.domain.RentalTool;
import com.example.quanly.domain.RentalToolStatus;
import com.example.quanly.domain.RentalType;
import com.example.quanly.domain.*;
import com.example.quanly.domain.dto.BookingResponseDTO;
import com.example.quanly.domain.dto.PendingBookingData;
import com.example.quanly.domain.dto.PreparedBookingResult;
import com.example.quanly.domain.dto.RentalItem;
import com.example.quanly.exception.BusinessConflictException;
import com.example.quanly.mapper.BookingMapper;
import com.example.quanly.repository.*;
import com.example.quanly.service.BookingService;
import com.example.quanly.service.NotificationService;
import com.example.quanly.service.PendingBookingCache;
import com.example.quanly.service.pricing.PricingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceCriticalTest {

    @Mock BookingRepository bookingRepository;
    @Mock BookingDetailRepository bookingDetailRepository;
    @Mock RentalToolRepository rentalToolRepository;
    @Mock RacketRepository racketRepository;
    @Mock UserRepository userRepository;
    @Mock ProductRepository productRepository;
    @Mock TimeRepository timeRepository;
    @Mock SubCourtRepository subCourtRepository;
    @Mock SubCourtAvailableTimeRepository subCourtAvailableTimeRepository;
    @Mock TemporaryBookingRepository temporaryBookingRepository;
    @Mock BookingMapper bookingMapper;
    @Mock PricingService pricingService;
    @Mock PendingBookingCache pendingBookingCache;
    @Mock NotificationService notificationService;
    @Mock ContactInfo contactInfo;

    @InjectMocks BookingService bookingService;

    @Test
    void bookingHistoryLookupIsScopedToCurrentUser() {
        Booking booking = new Booking();
        BookingResponseDTO dto = new BookingResponseDTO();
        when(bookingRepository.findByIdAndUserId(12L, 7L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toDTO(booking)).thenReturn(dto);

        assertSame(dto, bookingService.fetchBookingByIdAndUser(12L, 7L).orElseThrow());
        verify(bookingRepository).findByIdAndUserId(12L, 7L);
        verify(bookingRepository, never()).findById(12L);
    }

    @Test
    void bookingCannotBeCancelledThroughGenericStatusUpdate() {
        Booking booking = new Booking();
        booking.setId(4L);
        booking.setStatus(BookingStatus.DA_DAT_COC);
        when(bookingRepository.findById(4L)).thenReturn(Optional.of(booking));

        assertThrows(BusinessConflictException.class,
                () -> bookingService.updateBooking(4L, BookingStatus.DA_HUY.name()));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void completingBookingReturnsOnSiteRentalStockExactlyOnce() {
        Booking booking = new Booking();
        booking.setId(5L);
        booking.setStatus(BookingStatus.DA_DAT_COC);

        RentalTool rental = new RentalTool();
        rental.setType(RentalType.ON_SITE);
        rental.setStatus(RentalToolStatus.PAID);
        rental.setRacketId(9L);
        rental.setQuantity(2);

        Racket racket = new Racket();
        racket.setId(9L);
        racket.setBookingStockQuantity(3);

        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        when(rentalToolRepository.findRentalToolsByBookingId("5")).thenReturn(List.of(rental));
        when(racketRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(racket));

        bookingService.updateBooking(5L, BookingStatus.DA_THANH_TOAN.name());

        assertEquals(BookingStatus.DA_THANH_TOAN, booking.getStatus());
        assertEquals(RentalToolStatus.COMPLETED, rental.getStatus());
        assertEquals(5, racket.getBookingStockQuantity());
        verify(racketRepository).findByIdForUpdate(9L);
        verify(racketRepository).save(racket);
    }

    @Test
    void recurringBookingHoldsEveryDateBeforeStartingPayment() {
        LocalDate firstDate = LocalDate.now().plusDays(1);
        User user = new User();
        user.setId(7L);

        Product product = new Product();
        product.setId(2L);
        product.setPrice(200_000);
        product.setDepositPrice(50_000);

        SubCourt subCourt = new SubCourt();
        subCourt.setId(3L);
        subCourt.setProduct(product);

        AvailableTime time = new AvailableTime();
        time.setId(4L);
        time.setTime(LocalTime.of(10, 0));

        TemporaryBooking firstHold = new TemporaryBooking();
        firstHold.setId(20L);
        firstHold.setUserId(7L);
        firstHold.setSubCourt(subCourt);
        firstHold.setAvailableTime(time);
        firstHold.setBookingDate(firstDate);
        firstHold.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(3));

        when(userRepository.findUserById(7L)).thenReturn(user);
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(subCourtRepository.findById(3L)).thenReturn(Optional.of(subCourt));
        when(timeRepository.findById(4L)).thenReturn(Optional.of(time));
        when(subCourtAvailableTimeRepository.findBySubCourtAndAvailableTime(subCourt, time))
                .thenReturn(Optional.of(new SubCourtAvailableTime()));
        when(bookingDetailRepository.findBySubCourtAndAvailableTimeAndDate(
                eq(subCourt), eq(time), any(LocalDate.class))).thenReturn(Optional.empty());
        when(temporaryBookingRepository.findBySubCourtAndAvailableTimeAndBookingDateWithLock(
                eq(subCourt), eq(time), any(LocalDate.class)))
                .thenReturn(Optional.of(firstHold), Optional.empty(), Optional.empty());
        AtomicLong holdId = new AtomicLong(21);
        when(temporaryBookingRepository.saveAndFlush(any(TemporaryBooking.class))).thenAnswer(invocation -> {
            TemporaryBooking hold = invocation.getArgument(0);
            hold.setId(holdId.getAndIncrement());
            return hold;
        });
        when(pricingService.calculateFinalPrice(anyDouble(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(pendingBookingCache.store(any())).thenReturn(99L);

        bookingService.preparePendingBooking(
                user, "Receiver", "Address", "0901234567",
                2L, 4L, 3L, firstDate,
                "WEEKLY_RECURRING", firstDate.plusWeeks(2), List.of());

        ArgumentCaptor<PendingBookingData> captor = ArgumentCaptor.forClass(PendingBookingData.class);
        verify(pendingBookingCache).store(captor.capture());
        assertEquals(List.of(20L, 21L, 22L), captor.getValue().getTemporaryBookingIds());
        assertEquals(3, captor.getValue().getSlots().size());
        verify(temporaryBookingRepository, times(2)).saveAndFlush(any(TemporaryBooking.class));
    }

    @Test
    void bundledRentalIsIncludedInBothBookingTotalAndVnpayAmount() {
        LocalDate bookingDate = LocalDate.now().plusDays(1);
        User user = new User();
        user.setId(7L);

        Product product = new Product();
        product.setId(2L);
        product.setPrice(200_000);
        product.setSale(0);
        product.setDepositPrice(50_000);

        SubCourt subCourt = new SubCourt();
        subCourt.setId(3L);
        subCourt.setProduct(product);

        AvailableTime time = new AvailableTime();
        time.setId(4L);
        time.setTime(LocalTime.of(10, 0));

        TemporaryBooking hold = new TemporaryBooking();
        hold.setId(20L);
        hold.setUserId(7L);
        hold.setSubCourt(subCourt);
        hold.setAvailableTime(time);
        hold.setBookingDate(bookingDate);
        hold.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(3));

        Racket racket = new Racket();
        racket.setId(9L);
        racket.setName("Test racket");
        racket.setBookingStockQuantity(5);
        racket.setRentalPricePerPlay(30_000);

        RentalItem rentalItem = new RentalItem();
        rentalItem.setRacketId(9L);
        rentalItem.setQuantity(2);

        when(userRepository.findUserById(7L)).thenReturn(user);
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(subCourtRepository.findById(3L)).thenReturn(Optional.of(subCourt));
        when(timeRepository.findById(4L)).thenReturn(Optional.of(time));
        when(subCourtAvailableTimeRepository.findBySubCourtAndAvailableTime(subCourt, time))
                .thenReturn(Optional.of(new SubCourtAvailableTime()));
        when(bookingDetailRepository.findBySubCourtAndAvailableTimeAndDate(subCourt, time, bookingDate))
                .thenReturn(Optional.empty());
        when(temporaryBookingRepository.findBySubCourtAndAvailableTimeAndBookingDateWithLock(
                subCourt, time, bookingDate)).thenReturn(Optional.of(hold));
        when(pricingService.calculateFinalPrice(anyDouble(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(racketRepository.findById(9L)).thenReturn(Optional.of(racket));
        when(pendingBookingCache.store(any())).thenReturn(99L);

        PreparedBookingResult result = bookingService.preparePendingBooking(
                user, "Receiver", "Address", "0901234567",
                2L, 4L, 3L, bookingDate,
                "ONE_TIME", null, List.of(rentalItem));

        ArgumentCaptor<PendingBookingData> captor = ArgumentCaptor.forClass(PendingBookingData.class);
        verify(pendingBookingCache).store(captor.capture());
        PendingBookingData pending = captor.getValue();

        assertEquals(110_000, result.depositPrice(), 0.001);
        assertEquals(110_000, pending.getDepositPrice(), 0.001);
        assertEquals(260_000, pending.getTotalBookingPrice(), 0.001);
        assertEquals(60_000, pending.getRentalSlots().get(0).getSubtotal(), 0.001);
    }
}
