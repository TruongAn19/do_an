package com.example.quanly;

import com.example.quanly.config.HoldPolicy;
import com.example.quanly.controller.client.BookingClientController;
import com.example.quanly.domain.*;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.domain.dto.HoldBookingRequest;
import com.example.quanly.repository.SubCourtRepository;
import com.example.quanly.repository.TemporaryBookingRepository;
import com.example.quanly.repository.TimeRepository;
import com.example.quanly.repository.UserRepository;
import com.example.quanly.repository.ProductRepository;
import com.example.quanly.util.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A6 — Tập trung hằng số '3 phút' giữ chỗ.
 * 
 * <p>Chạy với cấu hình riêng booking.hold.duration-minutes=5 để kiểm chứng tính năng động.
 */
@SpringBootTest(properties = "booking.hold.duration-minutes=5")
@ActiveProfiles("test")
class BookingHoldExpiryTest {

    @Autowired BookingClientController bookingClientController;
    @Autowired TemporaryBookingRepository temporaryBookingRepository;
    @Autowired UserRepository userRepository;
    @Autowired ProductRepository productRepository;
    @Autowired SubCourtRepository subCourtRepository;
    @Autowired TimeRepository timeRepository;
    @Autowired HoldPolicy holdPolicy;

    @MockBean SecurityUtils securityUtils; // mock để lấy user hiện tại mong muốn

    private User user;
    private Product product;
    private SubCourt subCourt;
    private List<AvailableTime> times;

    private static final LocalDate SLOT_DATE = LocalDate.of(2026, 9, 20);

    @BeforeEach
    void seed() {
        user = new User();
        user.setEmail("a6test@example.com");
        user.setPassword("secret");
        user.setFullName("A6 Tester");
        user.setPhone("0966666666");
        user = userRepository.save(user);

        // Mock SecurityUtils.getCurrentUser() trả về user vừa tạo
        Mockito.when(securityUtils.getCurrentUser()).thenReturn(user);

        product = new Product();
        product.setName("San cau long A6");
        product.setPrice(100000);
        product = productRepository.save(product);

        subCourt = new SubCourt();
        subCourt.setName("San A6");
        subCourt.setProduct(product);
        subCourt = subCourtRepository.save(subCourt);

        times = new ArrayList<>();
        AvailableTime time = new AvailableTime();
        time.setTime(LocalTime.of(8, 0));
        times.add(timeRepository.save(time));
    }

    @AfterEach
    void cleanup() {
        temporaryBookingRepository.deleteAll();
        subCourtRepository.deleteAll();
        timeRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    private HoldBookingRequest holdRequest(long subCourtId, long timeId, LocalDate date) {
        HoldBookingRequest req = new HoldBookingRequest();
        req.setSubCourtId(subCourtId);
        req.setAvailableTimeId(timeId);
        req.setBookingDate(date);
        return req;
    }

    @Test
    @DisplayName("Cấu hình giữ chỗ động: Đảm bảo hold duration minutes được nạp đúng = 5")
    void testHoldDurationMinutesConfiguration() {
        assertEquals(5, holdPolicy.getDurationMinutes());
    }

    @Test
    @DisplayName("Giữ chỗ với cấu hình động: Thời gian còn lại trả về cho FE phải là 300 giây")
    void testHoldCourtDynamicRemainingTime() {
        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                bookingClientController.holdCourt(holdRequest(subCourt.getId(), times.get(0).getId(), SLOT_DATE));
        assertEquals(200, response.getStatusCode().value());
        
        Map<String, Object> data = response.getBody().getData();
        assertNotNull(data);
        assertEquals(300L, ((Number) data.get("remainingTime")).longValue());
    }

    @Test
    @DisplayName("Kiểm tra hết hạn (isExpired) động: Hold 4 phút chưa hết hạn, 6 phút đã hết hạn")
    void testHoldExpirationDynamic() {
        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                bookingClientController.holdCourt(holdRequest(subCourt.getId(), times.get(0).getId(), SLOT_DATE));
        assertEquals(200, response.getStatusCode().value());

        List<TemporaryBooking> holds = temporaryBookingRepository.findAll();
        assertEquals(1, holds.size());
        TemporaryBooking hold = holds.get(0);

        // Trường hợp 1: Mới trôi qua 4 phút (chưa đạt ngưỡng 5 phút) -> chưa expired
        hold.setHoldStartTime(LocalDateTime.now().minusMinutes(4));
        temporaryBookingRepository.saveAndFlush(hold);
        assertFalse(hold.isExpired(holdPolicy.getHoldDuration()));

        // Trường hợp 2: Trôi qua 6 phút (vượt ngưỡng 5 phút) -> expired
        hold.setHoldStartTime(LocalDateTime.now().minusMinutes(6));
        temporaryBookingRepository.saveAndFlush(hold);
        assertTrue(hold.isExpired(holdPolicy.getHoldDuration()));
    }
}
