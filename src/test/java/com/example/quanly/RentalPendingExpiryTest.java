package com.example.quanly;

import com.example.quanly.domain.Product;
import com.example.quanly.domain.Racket;
import com.example.quanly.domain.RacketStockByDate;
import com.example.quanly.domain.RentalTool;
import com.example.quanly.domain.RentalToolStatus;
import com.example.quanly.domain.RentalType;
import com.example.quanly.domain.User;
import com.example.quanly.repository.NotificationRepository;
import com.example.quanly.repository.ProductRepository;
import com.example.quanly.repository.RacketRepository;
import com.example.quanly.repository.RacketStockByDateRepository;
import com.example.quanly.repository.RentalToolRepository;
import com.example.quanly.repository.UserRepository;
import com.example.quanly.service.RentalToolCleaner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * B3 — Job tự huỷ đơn thuê vợt DAILY PENDING quá hạn thanh toán.
 *
 * <p>Pattern giống {@link RentalCancelTest}: H2 thật (profile "test"), KHÔNG {@code @Transactional},
 * dọn thủ công {@link #cleanup()}. Mặc định {@code rental.pending.expiry-minutes=30}; test set
 * {@code createAt} cũ/mới rõ ràng để không phụ thuộc con số chính xác.
 */
@SpringBootTest
@ActiveProfiles("test")
class RentalPendingExpiryTest {

    @Autowired RentalToolCleaner rentalToolCleaner;
    @Autowired RentalToolRepository rentalToolRepository;
    @Autowired RacketRepository racketRepository;
    @Autowired RacketStockByDateRepository racketStockByDateRepository;
    @Autowired ProductRepository productRepository;
    @Autowired UserRepository userRepository;
    @Autowired NotificationRepository notificationRepository;

    private User user;
    private Racket racket;

    private static final LocalDate FUTURE = LocalDate.of(2026, 11, 10);
    private static final LocalDateTime OLD = LocalDateTime.now().minusHours(2);   // quá hạn (>30')
    private static final LocalDateTime FRESH = LocalDateTime.now();               // còn hạn

    @BeforeEach
    void seed() {
        user = new User();
        user.setEmail("b3test@example.com");
        user.setPassword("secret");
        user.setFullName("B3 Tester");
        user.setPhone("0913000000");
        user = userRepository.save(user);

        Product product = new Product();
        product.setName("San cau long B3");
        product.setPrice(100000);
        product = productRepository.save(product);

        racket = new Racket();
        racket.setName("Yonex Duora");
        racket.setPrice(500000);
        racket.setRentalPricePerPlay(30000);
        racket.setAvailable(true);
        racket.setProduct(product);
        racket.setBookingStockQuantity(5);
        racket = racketRepository.save(racket);
    }

    @AfterEach
    void cleanup() {
        rentalToolRepository.deleteAll();
        racketStockByDateRepository.deleteAll();
        notificationRepository.deleteAll();
        racketRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ------------------------------------------------------------------ helpers

    private RentalTool rental(RentalToolStatus status, RentalType type, LocalDateTime createAt) {
        RentalTool rt = new RentalTool();
        rt.setType(type);
        rt.setStatus(status);
        rt.setRacketId(racket.getId());
        rt.setUserId(user.getId());
        rt.setQuantity(1);
        rt.setQuantityDay(1);
        rt.setRentalDate(FUTURE);
        rt.setFullName("B3 Tester");
        rt.setEmail("b3test@example.com");
        rt.setPhone("0913000000");
        rt.setCreateAt(createAt);
        rt.setUpdateAt(createAt);
        return rentalToolRepository.save(rt);
    }

    private RentalToolStatus statusOf(Long id) {
        return rentalToolRepository.findById(id).orElseThrow().getStatus();
    }

    // ------------------------------------------------------------------ tests

    @Test
    @DisplayName("Rental PENDING DAILY quá hạn → job chuyển CANCELLED")
    void pendingDailyExpired_isCancelled() {
        RentalTool rt = rental(RentalToolStatus.PENDING, RentalType.DAILY, OLD);

        rentalToolCleaner.expirePendingRentals();

        assertEquals(RentalToolStatus.CANCELLED, statusOf(rt.getId()));
    }

    @Test
    @DisplayName("Rental PENDING DAILY còn hạn → KHÔNG bị đụng")
    void pendingDailyFresh_isUntouched() {
        RentalTool rt = rental(RentalToolStatus.PENDING, RentalType.DAILY, FRESH);

        rentalToolCleaner.expirePendingRentals();

        assertEquals(RentalToolStatus.PENDING, statusOf(rt.getId()));
    }

    @Test
    @DisplayName("Rental IN_USE quá hạn → KHÔNG bị đụng (job chỉ lọc PENDING), kho giữ nguyên")
    void inUseExpired_isUntouched() {
        // stock đã trừ kiểu IN_USE: available giảm, reserved giữ q
        RacketStockByDate s = new RacketStockByDate();
        s.setRacketId(racket.getId());
        s.setDate(FUTURE);
        s.setAvailableStock(4);
        s.setReservedStock(1);
        s.setRentalStock(0);
        s.setTotalStock(5);
        racketStockByDateRepository.save(s);

        RentalTool rt = rental(RentalToolStatus.IN_USE, RentalType.DAILY, OLD);

        rentalToolCleaner.expirePendingRentals();

        assertEquals(RentalToolStatus.IN_USE, statusOf(rt.getId()));
        RacketStockByDate after = racketStockByDateRepository
                .findByRacketIdAndDate(racket.getId(), FUTURE).orElseThrow();
        assertEquals(4, after.getAvailableStock(), "Kho IN_USE không được job đụng");
        assertEquals(1, after.getReservedStock());
    }

    @Test
    @DisplayName("Rental ON_SITE PENDING quá hạn → KHÔNG bị đụng (job chỉ lọc DAILY)")
    void onSitePendingExpired_isUntouched() {
        RentalTool rt = rental(RentalToolStatus.PENDING, RentalType.ON_SITE, OLD);

        rentalToolCleaner.expirePendingRentals();

        assertEquals(RentalToolStatus.PENDING, statusOf(rt.getId()));
    }

    @Test
    @DisplayName("Lô hỗn hợp: chỉ PENDING-DAILY-quá-hạn bị huỷ, các đơn còn lại giữ nguyên")
    void mixedBatch_onlyExpiredPendingDailyCancelled() {
        RentalTool expiredDaily = rental(RentalToolStatus.PENDING, RentalType.DAILY, OLD);
        RentalTool freshDaily = rental(RentalToolStatus.PENDING, RentalType.DAILY, FRESH);
        RentalTool onSiteOld = rental(RentalToolStatus.PENDING, RentalType.ON_SITE, OLD);
        RentalTool inUseOld = rental(RentalToolStatus.IN_USE, RentalType.DAILY, OLD);

        rentalToolCleaner.expirePendingRentals();

        assertEquals(RentalToolStatus.CANCELLED, statusOf(expiredDaily.getId()));
        assertEquals(RentalToolStatus.PENDING, statusOf(freshDaily.getId()));
        assertEquals(RentalToolStatus.PENDING, statusOf(onSiteOld.getId()));
        assertEquals(RentalToolStatus.IN_USE, statusOf(inUseOld.getId()));
    }
}
