package com.example.quanly;

import com.example.quanly.domain.*;
import com.example.quanly.domain.dto.CreateRentalRequest;
import com.example.quanly.exception.ForbiddenOperationException;
import com.example.quanly.repository.*;
import com.example.quanly.service.BookingService;
import com.example.quanly.service.RentalToolService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class RentalCriticalLogicTest {

    @Autowired RentalToolService rentalToolService;
    @Autowired BookingService bookingService;
    @Autowired RentalToolRepository rentalToolRepository;
    @Autowired RacketRepository racketRepository;
    @Autowired RacketStockByDateRepository stockRepository;
    @Autowired BookingRepository bookingRepository;
    @Autowired BookingDetailRepository bookingDetailRepository;
    @Autowired ProductRepository productRepository;
    @Autowired UserRepository userRepository;
    @Autowired TimeRepository timeRepository;
    @Autowired NotificationRepository notificationRepository;

    private User owner;
    private User otherUser;
    private Product bookedProduct;
    private Product otherProduct;
    private Racket bookedProductRacket;
    private Racket otherProductRacket;
    private Booking booking;

    @BeforeEach
    void seed() {
        owner = user("critical-owner@example.com");
        otherUser = user("critical-other@example.com");
        bookedProduct = product("Critical court");
        otherProduct = product("Other court");
        bookedProductRacket = racket(bookedProduct, "Correct racket", 3);
        otherProductRacket = racket(otherProduct, "Wrong racket", 3);

        AvailableTime time = new AvailableTime();
        time.setTime(LocalTime.now().plusHours(2).withSecond(0).withNano(0));
        time = timeRepository.save(time);

        booking = new Booking();
        booking.setUser(owner);
        booking.setStatus(BookingStatus.DA_DAT);
        booking.setBookingDate(LocalDate.now().plusDays(1));
        booking.setAvailableTime(time);
        booking.setTotalPrice(100_000);
        booking = bookingRepository.save(booking);

        BookingDetail detail = new BookingDetail();
        detail.setBooking(booking);
        detail.setProduct(bookedProduct);
        detail.setDate(booking.getBookingDate());
        detail.setPrice(100_000);
        bookingDetailRepository.save(detail);
    }

    @AfterEach
    void cleanup() {
        rentalToolRepository.deleteAll();
        stockRepository.deleteAll();
        notificationRepository.deleteAll();
        bookingDetailRepository.deleteAll();
        bookingRepository.deleteAll();
        racketRepository.deleteAll();
        timeRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void dailyPaymentProcessedTwice_deductsStockOnce() {
        LocalDate date = LocalDate.now().plusDays(2);
        RacketStockByDate stock = new RacketStockByDate();
        stock.setRacketId(bookedProductRacket.getId());
        stock.setDate(date);
        stock.setAvailableStock(3);
        stock.setReservedStock(0);
        stock.setRentalStock(0);
        stock.setTotalStock(3);
        stockRepository.save(stock);

        RentalTool rental = dailyRental(date, 1);
        rentalToolService.handleDailyRental(rental.getId());
        rentalToolService.handleDailyRental(rental.getId());

        RacketStockByDate after = stockRepository
                .findByRacketIdAndDate(bookedProductRacket.getId(), date).orElseThrow();
        assertEquals(2, after.getAvailableStock());
        assertEquals(1, after.getReservedStock());
        assertEquals(RentalToolStatus.IN_USE,
                rentalToolRepository.findById(rental.getId()).orElseThrow().getStatus());
    }

    @Test
    void onSiteRentalForAnotherUsersBooking_isForbidden() {
        CreateRentalRequest request = onSiteRequest(bookedProductRacket, 1);

        assertThrows(ForbiddenOperationException.class,
                () -> rentalToolService.handleSubmitRental(request, otherUser));
        assertEquals(0, rentalToolRepository.count());
        assertEquals(3, racketRepository.findById(bookedProductRacket.getId())
                .orElseThrow().getBookingStockQuantity());
    }

    @Test
    void onSiteRentalWithRacketFromAnotherProduct_isRejected() {
        CreateRentalRequest request = onSiteRequest(otherProductRacket, 1);

        assertThrows(IllegalArgumentException.class,
                () -> rentalToolService.handleSubmitRental(request, owner));
        assertEquals(0, rentalToolRepository.count());
        assertEquals(3, racketRepository.findById(otherProductRacket.getId())
                .orElseThrow().getBookingStockQuantity());
    }

    @Test
    void onSiteRental_deductsStockAndBookingCancellationRestoresIt() {
        CreateRentalRequest request = onSiteRequest(bookedProductRacket, 2);

        rentalToolService.handleSubmitRental(request, owner);

        RentalTool rental = rentalToolRepository.findAll().get(0);
        assertEquals(RentalToolStatus.IN_USE, rental.getStatus());
        assertEquals(1, racketRepository.findById(bookedProductRacket.getId())
                .orElseThrow().getBookingStockQuantity());

        bookingService.cancelByUser(booking.getId(), owner.getId(), "Critical regression test");

        assertEquals(3, racketRepository.findById(bookedProductRacket.getId())
                .orElseThrow().getBookingStockQuantity());
        assertEquals(RentalToolStatus.CANCELLED,
                rentalToolRepository.findById(rental.getId()).orElseThrow().getStatus());
    }

    @Test
    void multipleOnSiteRentals_accumulateBookingTotal() {
        rentalToolService.handleSubmitRental(onSiteRequest(bookedProductRacket, 1), owner);
        rentalToolService.handleSubmitRental(onSiteRequest(bookedProductRacket, 1), owner);

        Booking after = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(160_000, after.getTotalPrice(), 0.01);
        assertEquals(2, rentalToolRepository.count());
        assertEquals(1, racketRepository.findById(bookedProductRacket.getId())
                .orElseThrow().getBookingStockQuantity());
    }

    private User user(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("secret");
        user.setFullName("Critical Tester");
        user.setPhone("0900000000");
        return userRepository.save(user);
    }

    private Product product(String name) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(100_000);
        return productRepository.save(product);
    }

    private Racket racket(Product product, String name, int stock) {
        Racket racket = new Racket();
        racket.setName(name);
        racket.setProduct(product);
        racket.setAvailable(true);
        racket.setPrice(500_000);
        racket.setRentalPricePerPlay(30_000);
        racket.setRentalPricePerDay(50_000);
        racket.setBookingStockQuantity(stock);
        return racketRepository.save(racket);
    }

    private CreateRentalRequest onSiteRequest(Racket racket, int quantity) {
        CreateRentalRequest request = new CreateRentalRequest();
        request.setFullName("Critical Tester");
        request.setEmail("critical@example.com");
        request.setPhone("0900000000");
        request.setType(RentalType.ON_SITE);
        request.setRacketId(racket.getId());
        request.setQuantity(quantity);
        request.setBookingCode(booking.getBookingCode());
        return request;
    }

    private RentalTool dailyRental(LocalDate date, int quantity) {
        RentalTool rental = new RentalTool();
        rental.setType(RentalType.DAILY);
        rental.setStatus(RentalToolStatus.PENDING);
        rental.setRacketId(bookedProductRacket.getId());
        rental.setProductId(bookedProduct.getId());
        rental.setUserId(owner.getId());
        rental.setQuantity(quantity);
        rental.setQuantityDay(1);
        rental.setRentalDate(date);
        rental.setCreateAt(LocalDateTime.now());
        rental.setUpdateAt(LocalDateTime.now());
        rental.setFullName("Critical Tester");
        rental.setEmail("critical@example.com");
        rental.setPhone("0900000000");
        return rentalToolRepository.save(rental);
    }
}
