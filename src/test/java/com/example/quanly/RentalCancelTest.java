package com.example.quanly;

import com.example.quanly.domain.Product;
import com.example.quanly.domain.Racket;
import com.example.quanly.domain.RacketStockByDate;
import com.example.quanly.domain.RefundStatus;
import com.example.quanly.domain.RentalTool;
import com.example.quanly.domain.RentalToolStatus;
import com.example.quanly.domain.RentalType;
import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.RentalToolDTO;
import com.example.quanly.exception.ResourceNotFoundException;
import com.example.quanly.repository.NotificationRepository;
import com.example.quanly.repository.ProductRepository;
import com.example.quanly.repository.RacketRepository;
import com.example.quanly.repository.RacketStockByDateRepository;
import com.example.quanly.repository.RentalToolRepository;
import com.example.quanly.repository.UserRepository;
import com.example.quanly.service.RentalToolService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * B1 — Huỷ đơn thuê vợt standalone (DAILY) + hoàn tồn kho {@link RacketStockByDate}.
 *
 * <p>Cùng pattern với {@link RentalStockConfirmTest}: H2 thật (profile "test"), KHÔNG
 * {@code @Transactional} (cần commit thật), dọn dữ liệu thủ công ở {@link #cleanup()}.
 */
@SpringBootTest
@ActiveProfiles("test")
class RentalCancelTest {

    @Autowired RentalToolService rentalToolService;
    @Autowired RentalToolRepository rentalToolRepository;
    @Autowired RacketRepository racketRepository;
    @Autowired RacketStockByDateRepository racketStockByDateRepository;
    @Autowired ProductRepository productRepository;
    @Autowired UserRepository userRepository;
    @Autowired NotificationRepository notificationRepository;

    private User user;
    private Racket racket;

    private static final LocalDate FUTURE = LocalDate.of(2026, 9, 15);

    @BeforeEach
    void seed() {
        user = new User();
        user.setEmail("b1test@example.com");
        user.setPassword("secret");
        user.setFullName("B1 Tester");
        user.setPhone("0911111111");
        user = userRepository.save(user);

        Product product = new Product();
        product.setName("San cau long B1");
        product.setPrice(100000);
        product = productRepository.save(product);

        racket = new Racket();
        racket.setName("Yonex Nanoflare");
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

    private RacketStockByDate stock(LocalDate date, int available, int reserved, int rental) {
        RacketStockByDate s = new RacketStockByDate();
        s.setRacketId(racket.getId());
        s.setDate(date);
        s.setAvailableStock(available);
        s.setReservedStock(reserved);
        s.setRentalStock(rental);
        s.setTotalStock(available + reserved + rental);
        return racketStockByDateRepository.save(s);
    }

    private RentalTool rental(RentalToolStatus status, RentalType type,
                              LocalDate rentalDate, int quantity, Integer quantityDay) {
        RentalTool rt = new RentalTool();
        rt.setType(type);
        rt.setStatus(status);
        rt.setRacketId(racket.getId());
        rt.setUserId(user.getId());
        rt.setQuantity(quantity);
        rt.setQuantityDay(quantityDay);
        rt.setRentalDate(rentalDate);
        rt.setFullName("B1 Tester");
        rt.setEmail("b1test@example.com");
        rt.setPhone("0911111111");
        rt.setCreateAt(LocalDateTime.now());
        rt.setUpdateAt(LocalDateTime.now());
        return rentalToolRepository.save(rt);
    }

    private RacketStockByDate reload(LocalDate date) {
        return racketStockByDateRepository.findByRacketIdAndDate(racket.getId(), date).orElseThrow();
    }

    private void assertStock(LocalDate date, int available, int reserved, int rental) {
        RacketStockByDate s = reload(date);
        assertEquals(available, s.getAvailableStock(), "availableStock ngày " + date);
        assertEquals(reserved, s.getReservedStock(), "reservedStock ngày " + date);
        assertEquals(rental, s.getRentalStock(), "rentalStock ngày " + date);
    }

    // ------------------------------------------------------------------ tests

    @Test
    @DisplayName("Không tìm thấy đơn -> ResourceNotFoundException")
    void cancel_notFound_throws() {
        assertThrows(ResourceNotFoundException.class, () -> rentalToolService.cancelRental(999_999L));
    }

    @Test
    @DisplayName("Huỷ IN_USE 1 ngày (bucket reservedStock) -> hoàn về availableStock đúng")
    void cancel_inUse_reservedBucket_restored() {
        // handleDailyRental cho ngày tương lai: available-=q, reserved+=q (chưa chuyển sang rental)
        stock(FUTURE, 4, 1, 0);
        RentalTool rt = rental(RentalToolStatus.IN_USE, RentalType.DAILY, FUTURE, 1, 1);

        rentalToolService.cancelRental(rt.getId());

        assertStock(FUTURE, 5, 0, 0);
        RentalTool after = rentalToolRepository.findById(rt.getId()).orElseThrow();
        assertEquals(RentalToolStatus.CANCELLED, after.getStatus());
        // Đơn đã thanh toán (IN_USE) bị huỷ -> phát sinh cọc chờ hoàn
        assertEquals(RefundStatus.PENDING_REFUND, after.getRefundStatus());
        assertEquals(java.time.LocalDate.now(), after.getCancelledAt().toLocalDate());
    }

    @Test
    @DisplayName("Huỷ IN_USE 2 ngày: hoàn đúng TỪNG ngày, gồm ca ngày hôm nay đã chuyển sang rentalStock")
    void cancel_inUse_perDay_includingRentalStockToday() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        // Ngày hôm nay: đã được handleDailyRental/cron chuyển reserved -> rental (reserved=0, rental=1)
        stock(today, 4, 0, 1);
        // Ngày mai: vẫn ở reserved (reserved=1, rental=0)
        stock(tomorrow, 4, 1, 0);

        RentalTool rt = rental(RentalToolStatus.IN_USE, RentalType.DAILY, today, 1, 2);

        rentalToolService.cancelRental(rt.getId());

        // Hôm nay: phần thiếu ở reserved (0) lấy từ rentalStock -> available+1, rental-1
        assertStock(today, 5, 0, 0);
        // Ngày mai: gỡ thẳng từ reserved -> available+1, reserved-1
        assertStock(tomorrow, 5, 0, 0);
        assertEquals(RentalToolStatus.CANCELLED, rentalToolRepository.findById(rt.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Huỷ PENDING -> chỉ đổi trạng thái, KHÔNG đụng kho")
    void cancel_pending_doesNotTouchStock() {
        stock(FUTURE, 4, 0, 0);
        RentalTool rt = rental(RentalToolStatus.PENDING, RentalType.DAILY, FUTURE, 1, 1);

        rentalToolService.cancelRental(rt.getId());

        assertStock(FUTURE, 4, 0, 0);
        RentalTool after = rentalToolRepository.findById(rt.getId()).orElseThrow();
        assertEquals(RentalToolStatus.CANCELLED, after.getStatus());
        // PENDING chưa thu tiền -> không phát sinh cọc cần hoàn
        assertEquals(RefundStatus.NOT_APPLICABLE, after.getRefundStatus());
    }

    @Test
    @DisplayName("Huỷ COMPLETED -> IllegalStateException, kho không đổi")
    void cancel_completed_blocked() {
        stock(FUTURE, 5, 0, 0);
        RentalTool rt = rental(RentalToolStatus.COMPLETED, RentalType.DAILY, FUTURE, 1, 1);

        assertThrows(IllegalStateException.class, () -> rentalToolService.cancelRental(rt.getId()));

        assertStock(FUTURE, 5, 0, 0);
        assertEquals(RentalToolStatus.COMPLETED, rentalToolRepository.findById(rt.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Gọi huỷ lần hai (đã CANCELLED) -> idempotent, không lỗi, không hoàn kho lần hai")
    void cancel_twice_isIdempotent() {
        stock(FUTURE, 4, 1, 0);
        RentalTool rt = rental(RentalToolStatus.IN_USE, RentalType.DAILY, FUTURE, 1, 1);

        rentalToolService.cancelRental(rt.getId());
        assertStock(FUTURE, 5, 0, 0);

        // Lần hai: không ném, kho giữ nguyên (không +q thêm lần nữa)
        assertDoesNotThrow(() -> rentalToolService.cancelRental(rt.getId()));
        assertStock(FUTURE, 5, 0, 0);
        assertEquals(RentalToolStatus.CANCELLED, rentalToolRepository.findById(rt.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Admin set CANCELLED qua changeStatus -> cũng đi qua đường hoàn kho")
    void changeStatus_cancelled_routesThroughCancelRental() {
        stock(FUTURE, 4, 1, 0);
        RentalTool rt = rental(RentalToolStatus.IN_USE, RentalType.DAILY, FUTURE, 1, 1);

        RentalToolDTO dto = rentalToolService.changeStatus(rt.getId(), RentalToolStatus.CANCELLED);

        assertEquals(RentalToolStatus.CANCELLED.name(), dto.getStatus());
        assertStock(FUTURE, 5, 0, 0);
    }
}
