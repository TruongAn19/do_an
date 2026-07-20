package com.example.quanly;

import com.example.quanly.domain.Booking;
import com.example.quanly.domain.BookingStatus;
import com.example.quanly.domain.User;
import com.example.quanly.mapper.UserMapper;
import com.example.quanly.repository.BookingRepository;
import com.example.quanly.repository.RentalToolRepository;
import com.example.quanly.repository.UserRepository;
import com.example.quanly.service.BookingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class MemberLevelUpgradeTest {

    @Autowired BookingService bookingService;
    @Autowired BookingRepository bookingRepository;
    @Autowired RentalToolRepository rentalToolRepository;
    @Autowired UserRepository userRepository;
    @Autowired UserMapper userMapper;

    private User user;

    @BeforeEach
    void seed() {
        user = new User();
        user.setEmail("membership@example.com");
        user.setPassword("secret");
        user.setFullName("Membership Tester");
        user.setPhone("0900000000");
        user.setMemberLevel("NORMAL");
        user = userRepository.save(user);
    }

    @AfterEach
    void cleanup() {
        rentalToolRepository.deleteAll();
        bookingRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void upgradesFromNormalToSilverAndGoldUsingFullyPaidTotal() {
        Booking unpaid = booking(10_000_000);
        assertEquals("NORMAL", reloadLevel(),
                "Booking chưa thanh toán đầy đủ không được tính hạng");

        Booking first = booking(1_000_000);
        bookingService.updateBooking(first.getId(), BookingStatus.DA_THANH_TOAN.getLabel());
        assertEquals("NORMAL", reloadLevel());

        Booking second = booking(1_000_000);
        bookingService.updateBooking(second.getId(), BookingStatus.DA_THANH_TOAN.getLabel());
        assertEquals("SILVER", reloadLevel());

        Booking third = booking(3_000_000);
        bookingService.updateBooking(third.getId(), BookingStatus.DA_THANH_TOAN.getLabel());
        assertEquals("GOLD", reloadLevel());

        bookingService.updateBooking(third.getId(), BookingStatus.DA_THANH_TOAN.getLabel());
        assertEquals("GOLD", reloadLevel(),
                "Cập nhật lại cùng booking phải idempotent, không làm sai hạng");

        assertEquals("GOLD", userMapper.toDTO(userRepository.findById(user.getId())
                .orElseThrow()).getMemberLevel());
        assertEquals(BookingStatus.DA_DAT,
                bookingRepository.findById(unpaid.getId()).orElseThrow().getStatus());
    }

    private Booking booking(double totalPrice) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setBookingDate(LocalDate.now().plusDays(1));
        booking.setStatus(BookingStatus.DA_DAT);
        booking.setTotalPrice(totalPrice);
        return bookingRepository.save(booking);
    }

    private String reloadLevel() {
        return userRepository.findById(user.getId()).orElseThrow().getMemberLevel();
    }
}
