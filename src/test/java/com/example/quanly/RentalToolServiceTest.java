package com.example.quanly;

import com.example.quanly.domain.RacketStockByDate;
import com.example.quanly.domain.Booking;
import com.example.quanly.domain.BookingDetail;
import com.example.quanly.domain.Product;
import com.example.quanly.domain.Racket;
import com.example.quanly.domain.RentalTool;
import com.example.quanly.domain.RentalToolStatus;
import com.example.quanly.domain.RentalType;
import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.CreateRentalRequest;
import com.example.quanly.exception.ForbiddenOperationException;
import com.example.quanly.mapper.RentalToolMapper;
import com.example.quanly.repository.BookingDetailRepository;
import com.example.quanly.repository.BookingRepository;
import com.example.quanly.repository.RacketRepository;
import com.example.quanly.repository.RacketStockByDateRepository;
import com.example.quanly.repository.RentalToolRepository;
import com.example.quanly.repository.UserRepository;
import com.example.quanly.service.RentalPricingService;
import com.example.quanly.service.RentalToolService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalToolServiceTest {

    @Mock RentalToolRepository rentalToolRepository;
    @Mock RacketRepository racketRepository;
    @Mock BookingRepository bookingRepository;
    @Mock RacketStockByDateRepository racketStockByDateRepository;
    @Mock UserRepository userRepository;
    @Mock BookingDetailRepository bookingDetailRepository;
    @Mock RentalToolMapper rentalToolMapper;
    @Mock RentalPricingService rentalPricingService;

    @InjectMocks RentalToolService rentalToolService;

    @Test
    void repeatedPaymentConfirmationDoesNotDeductStockTwice() {
        LocalDate rentalDate = LocalDate.now().plusDays(2);

        RentalTool rentalTool = new RentalTool();
        rentalTool.setId(1L);
        rentalTool.setType(RentalType.DAILY);
        rentalTool.setStatus(RentalToolStatus.PENDING);
        rentalTool.setRacketId(5L);
        rentalTool.setRentalDate(rentalDate);
        rentalTool.setQuantity(2);
        rentalTool.setQuantityDay(1);

        RacketStockByDate stock = new RacketStockByDate();
        stock.setRacketId(5L);
        stock.setDate(rentalDate);
        stock.setAvailableStock(10);

        when(rentalToolRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(rentalTool));
        when(racketStockByDateRepository.findByRacketIdAndDateForUpdate(5L, rentalDate))
                .thenReturn(Optional.of(stock));

        rentalToolService.confirmDailyRentalPayment(1L);
        rentalToolService.confirmDailyRentalPayment(1L);

        assertEquals(RentalToolStatus.RENTING, rentalTool.getStatus());
        assertEquals(8, stock.getAvailableStock());
        assertEquals(2, stock.getReservedStock());
        verify(racketStockByDateRepository, times(1)).save(stock);
    }

    @Test
    void paymentRechecksStockWhileHoldingDatabaseLock() {
        LocalDate rentalDate = LocalDate.now().plusDays(2);

        RentalTool rentalTool = new RentalTool();
        rentalTool.setId(2L);
        rentalTool.setType(RentalType.DAILY);
        rentalTool.setStatus(RentalToolStatus.PENDING);
        rentalTool.setRacketId(5L);
        rentalTool.setRentalDate(rentalDate);
        rentalTool.setQuantity(2);
        rentalTool.setQuantityDay(1);

        RacketStockByDate stock = new RacketStockByDate();
        stock.setAvailableStock(1);

        when(rentalToolRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(rentalTool));
        when(racketStockByDateRepository.findByRacketIdAndDateForUpdate(5L, rentalDate))
                .thenReturn(Optional.of(stock));

        assertThrows(com.example.quanly.exception.BusinessConflictException.class,
                () -> rentalToolService.confirmDailyRentalPayment(2L));

        assertEquals(1, stock.getAvailableStock());
        assertEquals(0, stock.getReservedStock());
        verify(racketStockByDateRepository, times(0)).save(stock);
    }

    @Test
    void onSiteRentalCannotBeAttachedToAnotherUsersBooking() {
        User currentUser = new User();
        currentUser.setId(10L);

        User bookingOwner = new User();
        bookingOwner.setId(20L);

        Product product = new Product();
        product.setId(3L);

        Racket racket = new Racket();
        racket.setId(5L);
        racket.setProduct(product);
        racket.setPrice(3_000_000D);

        Booking booking = new Booking();
        booking.setUser(bookingOwner);

        CreateRentalRequest request = new CreateRentalRequest();
        request.setType(RentalType.ON_SITE);
        request.setRacketId(5L);
        request.setQuantity(1);
        request.setBookingCode("BK-OTHER-USER");

        when(racketRepository.findById(5L)).thenReturn(Optional.of(racket));
        when(userRepository.findUserById(10L)).thenReturn(currentUser);
        when(bookingRepository.findByBookingCode("BK-OTHER-USER")).thenReturn(booking);

        assertThrows(ForbiddenOperationException.class,
                () -> rentalToolService.handleSubmitRental(request, currentUser));
    }

    @Test
    void onSiteRentalDeductsBookingStock() {
        User currentUser = new User();
        currentUser.setId(10L);

        Product product = new Product();
        product.setId(3L);

        Racket racket = new Racket();
        racket.setId(5L);
        racket.setName("Test racket");
        racket.setProduct(product);
        racket.setPrice(3_000_000D);
        racket.setBookingStockQuantity(5);

        Booking booking = new Booking();
        booking.setId(7L);
        booking.setUser(currentUser);
        booking.setBookingDate(LocalDate.now().plusDays(1));

        BookingDetail bookingDetail = new BookingDetail();
        bookingDetail.setPrice(500_000D);

        RentalTool existingRental = new RentalTool();
        existingRental.setStatus(RentalToolStatus.PAID);
        existingRental.setRentalPrice(100_000D);

        CreateRentalRequest request = new CreateRentalRequest();
        request.setType(RentalType.ON_SITE);
        request.setRacketId(5L);
        request.setQuantity(2);
        request.setBookingCode("BK-OWNER");

        when(racketRepository.findById(5L)).thenReturn(Optional.of(racket));
        when(racketRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(racket));
        when(userRepository.findUserById(10L)).thenReturn(currentUser);
        when(bookingRepository.findByBookingCode("BK-OWNER")).thenReturn(booking);
        when(bookingDetailRepository.findByBookingId(7L)).thenReturn(List.of(bookingDetail));
        when(rentalToolRepository.findRentalToolsByBookingId("7"))
                .thenReturn(List.of(existingRental));
        when(rentalPricingService.totalPrice(RentalType.ON_SITE, racket, 2, 1))
                .thenReturn(50_000D);

        rentalToolService.handleSubmitRental(request, currentUser);

        assertEquals(3, racket.getBookingStockQuantity());
        assertEquals(650_000D, booking.getTotalPrice());
        verify(rentalToolRepository).save(org.mockito.ArgumentMatchers.argThat(
                saved -> saved.getStatus() == RentalToolStatus.PAID
                        && saved.getType() == RentalType.ON_SITE));
        verify(racketRepository).save(racket);
    }

    @Test
    void rentingRentalCanBeCompletedAndReturnsRentedStock() {
        LocalDate rentalDate = LocalDate.now();

        RentalTool rentalTool = new RentalTool();
        rentalTool.setId(9L);
        rentalTool.setType(RentalType.DAILY);
        rentalTool.setStatus(RentalToolStatus.RENTING);
        rentalTool.setRacketId(5L);
        rentalTool.setRentalDate(rentalDate);
        rentalTool.setQuantity(2);
        rentalTool.setQuantityDay(1);

        RacketStockByDate stock = new RacketStockByDate();
        stock.setRacketId(5L);
        stock.setDate(rentalDate);
        stock.setAvailableStock(8);
        stock.setRentalStock(2);

        when(rentalToolRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(rentalTool));
        when(racketStockByDateRepository.findByRacketIdAndDateForUpdate(5L, rentalDate))
                .thenReturn(Optional.of(stock));

        rentalToolService.completeRental(9L);

        assertEquals(RentalToolStatus.COMPLETED, rentalTool.getStatus());
        assertEquals(10, stock.getAvailableStock());
        assertEquals(0, stock.getRentalStock());
        assertEquals(0, stock.getReservedStock());
        verify(racketStockByDateRepository).save(stock);
    }

    @Test
    void completedRentalCannotBeReopened() {
        RentalTool rentalTool = new RentalTool();
        rentalTool.setId(20L);
        rentalTool.setType(RentalType.DAILY);
        rentalTool.setStatus(RentalToolStatus.COMPLETED);
        when(rentalToolRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(rentalTool));

        assertThrows(com.example.quanly.exception.BusinessConflictException.class,
                () -> rentalToolService.changeStatus(20L, RentalToolStatus.RENTING));
    }

    @Test
    void returnedToCompletedDoesNotReleaseStockTwice() {
        RentalTool rentalTool = new RentalTool();
        rentalTool.setId(21L);
        rentalTool.setType(RentalType.DAILY);
        rentalTool.setStatus(RentalToolStatus.RETURNED);
        when(rentalToolRepository.findByIdForUpdate(21L)).thenReturn(Optional.of(rentalTool));
        when(rentalToolRepository.save(rentalTool)).thenReturn(rentalTool);

        rentalToolService.changeStatus(21L, RentalToolStatus.COMPLETED);

        assertEquals(RentalToolStatus.COMPLETED, rentalTool.getStatus());
        verify(racketStockByDateRepository, times(0))
                .findByRacketIdAndDateForUpdate(
                        org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.any());
    }

    @Test
    void dailyStockSchedulerDoesNotProcessUnpaidPendingRentals() {
        when(rentalToolRepository.findByTypeAndStatusIn(
                RentalType.DAILY, List.of(RentalToolStatus.PAID, RentalToolStatus.RENTING)))
                .thenReturn(List.of());

        rentalToolService.updateRentalStockForToday();

        verify(rentalToolRepository).findByTypeAndStatusIn(
                RentalType.DAILY, List.of(RentalToolStatus.PAID, RentalToolStatus.RENTING));
    }

    @Test
    void failedPaymentCancelsPendingRental() {
        RentalTool rentalTool = new RentalTool();
        rentalTool.setId(11L);
        rentalTool.setStatus(RentalToolStatus.PENDING);
        when(rentalToolRepository.findByIdForUpdate(11L))
                .thenReturn(Optional.of(rentalTool));
        when(rentalToolRepository.save(rentalTool)).thenReturn(rentalTool);

        rentalToolService.cancelFailedRentalPayment(11L);

        assertEquals(RentalToolStatus.CANCELLED, rentalTool.getStatus());
        verify(rentalToolRepository).save(rentalTool);
    }

    @Test
    void schedulerDoesNotActivateSameRentalTwiceOnSameDay() {
        LocalDate today = LocalDate.now();
        RentalTool rental = new RentalTool();
        rental.setId(12L);
        rental.setType(RentalType.DAILY);
        rental.setStatus(RentalToolStatus.PAID);
        rental.setRacketId(5L);
        rental.setRentalDate(today);
        rental.setQuantity(2);
        rental.setQuantityDay(1);

        RacketStockByDate stock = new RacketStockByDate();
        stock.setReservedStock(4);

        when(rentalToolRepository.findByTypeAndStatusIn(
                RentalType.DAILY, List.of(RentalToolStatus.PAID, RentalToolStatus.RENTING)))
                .thenReturn(List.of(rental));
        when(racketStockByDateRepository.findByRacketIdAndDateForUpdate(5L, today))
                .thenReturn(Optional.of(stock));

        rentalToolService.updateRentalStockForToday();
        rentalToolService.updateRentalStockForToday();

        assertEquals(2, stock.getReservedStock());
        assertEquals(2, stock.getRentalStock());
        assertEquals(today, rental.getLastStockActivatedDate());
        verify(racketStockByDateRepository, times(1)).save(stock);
    }
}
