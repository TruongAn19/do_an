package com.example.quanly;

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
 * A5 — Giới hạn số hold active mỗi user.
 *
 * <p>Chạy với cấu hình riêng booking.hold.max-active-per-user=3.
 */
@SpringBootTest(properties = "booking.hold.max-active-per-user=3")
@ActiveProfiles("test")
class BookingHoldLimitTest {

    @Autowired BookingClientController bookingClientController;
    @Autowired TemporaryBookingRepository temporaryBookingRepository;
    @Autowired UserRepository userRepository;
    @Autowired ProductRepository productRepository;
    @Autowired SubCourtRepository subCourtRepository;
    @Autowired TimeRepository timeRepository;

    @MockBean SecurityUtils securityUtils; // mock để lấy user hiện tại mong muốn

    private User user;
    private Product product;
    private SubCourt subCourt;
    private List<AvailableTime> times;

    private static final LocalDate SLOT_DATE = LocalDate.of(2026, 9, 20);

    @BeforeEach
    void seed() {
        user = new User();
        user.setEmail("a5test@example.com");
        user.setPassword("secret");
        user.setFullName("A5 Tester");
        user.setPhone("0955555555");
        user = userRepository.save(user);

        // Mock SecurityUtils.getCurrentUser() trả về user vừa tạo
        Mockito.when(securityUtils.getCurrentUser()).thenReturn(user);

        product = new Product();
        product.setName("San cau long A5");
        product.setPrice(100000);
        product = productRepository.save(product);

        subCourt = new SubCourt();
        subCourt.setName("San A5");
        subCourt.setProduct(product);
        subCourt = subCourtRepository.save(subCourt);

        times = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            AvailableTime time = new AvailableTime();
            time.setTime(LocalTime.of(8 + i, 0));
            times.add(timeRepository.save(time));
        }
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

    // ------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Giới hạn hold active: Giữ 3 sân thành công, lần thứ 4 bị chặn 429")
    void holdLimitEnforced_blocksAfterMaxLimit() {
        // 1. Giữ slot 1 thành công
        ResponseEntity<ApiResponse<Map<String, Object>>> r1 =
                bookingClientController.holdCourt(holdRequest(subCourt.getId(), times.get(0).getId(), SLOT_DATE));
        assertEquals(200, r1.getStatusCode().value());
        assertEquals("Giữ sân tạm thời thành công", r1.getBody().getMessage());

        // 2. Giữ slot 2 thành công
        ResponseEntity<ApiResponse<Map<String, Object>>> r2 =
                bookingClientController.holdCourt(holdRequest(subCourt.getId(), times.get(1).getId(), SLOT_DATE));
        assertEquals(200, r2.getStatusCode().value());

        // 3. Giữ slot 3 thành công
        ResponseEntity<ApiResponse<Map<String, Object>>> r3 =
                bookingClientController.holdCourt(holdRequest(subCourt.getId(), times.get(2).getId(), SLOT_DATE));
        assertEquals(200, r3.getStatusCode().value());

        // Tổng cộng có 3 holds trong DB
        assertEquals(3, temporaryBookingRepository.count());

        // 4. Giữ slot 4 -> Phải bị chặn với mã lỗi 429 (TOO_MANY_REQUESTS)
        ResponseEntity<ApiResponse<Map<String, Object>>> r4 =
                bookingClientController.holdCourt(holdRequest(subCourt.getId(), times.get(3).getId(), SLOT_DATE));
        assertEquals(429, r4.getStatusCode().value());
        assertTrue(r4.getBody().getMessage().contains("Bạn đã đạt giới hạn tối đa"));
        
        // Số holds trong DB vẫn giữ nguyên là 3
        assertEquals(3, temporaryBookingRepository.count());
    }

    @Test
    @DisplayName("Gia hạn hold của chính user: Cho phép gia hạn kể cả khi đã đạt max limit")
    void renewOwnHold_succeedsEvenAtLimit() {
        // Đạt tối đa giới hạn (3 active holds)
        bookingClientController.holdCourt(holdRequest(subCourt.getId(), times.get(0).getId(), SLOT_DATE));
        bookingClientController.holdCourt(holdRequest(subCourt.getId(), times.get(1).getId(), SLOT_DATE));
        bookingClientController.holdCourt(holdRequest(subCourt.getId(), times.get(2).getId(), SLOT_DATE));
        assertEquals(3, temporaryBookingRepository.count());

        // Gia hạn slot 1 (gửi lại request giữ chỗ slot 1) -> Phải thành công (200), không bị chặn 429
        ResponseEntity<ApiResponse<Map<String, Object>>> renew =
                bookingClientController.holdCourt(holdRequest(subCourt.getId(), times.get(0).getId(), SLOT_DATE));
        assertEquals(200, renew.getStatusCode().value());
        assertEquals("Tiếp tục giữ sân tạm thời", renew.getBody().getMessage());
        assertEquals(3, temporaryBookingRepository.count(), "Không phát sinh hold mới");
    }

    @Test
    @DisplayName("Giới hạn hold active: Cho phép giữ slot mới sau khi 1 hold cũ hết hạn")
    void allowNewHold_afterOldHoldExpires() {
        // Giữ 3 slot thành công
        bookingClientController.holdCourt(holdRequest(subCourt.getId(), times.get(0).getId(), SLOT_DATE));
        bookingClientController.holdCourt(holdRequest(subCourt.getId(), times.get(1).getId(), SLOT_DATE));
        bookingClientController.holdCourt(holdRequest(subCourt.getId(), times.get(2).getId(), SLOT_DATE));
        assertEquals(3, temporaryBookingRepository.count());

        // Giả lập slot 1 hết hạn bằng cách đổi holdStartTime lùi về 5 phút trước
        List<TemporaryBooking> holds = temporaryBookingRepository.findAll();
        TemporaryBooking expiredHold = holds.stream()
                .filter(h -> h.getAvailableTime().getId().equals(times.get(0).getId()))
                .findFirst().orElseThrow();
        expiredHold.setHoldStartTime(LocalDateTime.now().minusMinutes(5));
        temporaryBookingRepository.saveAndFlush(expiredHold);

        // Giữ slot 4 -> Phải thành công vì slot 1 hết hạn sẽ bị delete và không được tính là active
        ResponseEntity<ApiResponse<Map<String, Object>>> r4 =
                bookingClientController.holdCourt(holdRequest(subCourt.getId(), times.get(3).getId(), SLOT_DATE));
        assertEquals(200, r4.getStatusCode().value());
        assertEquals("Giữ sân tạm thời thành công", r4.getBody().getMessage());

        // Tổng số holds trong DB vẫn là 3 (vì slot 1 đã bị delete khi clean expired holds)
        assertEquals(3, temporaryBookingRepository.count());
        assertFalse(temporaryBookingRepository.findById(expiredHold.getId()).isPresent(), "Hold cũ hết hạn đã bị xoá");
    }
}
