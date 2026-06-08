package com.example.quanly;

import com.example.quanly.domain.NotificationType;
import com.example.quanly.domain.Product;
import com.example.quanly.domain.Racket;
import com.example.quanly.domain.RacketStockByDate;
import com.example.quanly.domain.RefundStatus;
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
import com.example.quanly.service.NotificationService;
import com.example.quanly.service.RentalToolService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * B-refund — Admin xác nhận hoàn cọc cho đơn thuê vợt + scope notify admin khi huỷ.
 *
 * <p>H2 thật (profile "test"), KHÔNG {@code @Transactional}, dọn thủ công. {@code @SpyBean}
 * NotificationService để kiểm chứng admin chỉ được báo khi huỷ đơn đã thanh toán (IN_USE).
 */
@SpringBootTest
@ActiveProfiles("test")
class RentalRefundTest {

    @Autowired RentalToolService rentalToolService;
    @Autowired RentalToolRepository rentalToolRepository;
    @Autowired RacketRepository racketRepository;
    @Autowired RacketStockByDateRepository racketStockByDateRepository;
    @Autowired ProductRepository productRepository;
    @Autowired UserRepository userRepository;
    @Autowired NotificationRepository notificationRepository;

    @SpyBean NotificationService notificationService;

    private User user;
    private Racket racket;

    private static final LocalDate FUTURE = LocalDate.of(2026, 12, 1);

    @BeforeEach
    void seed() {
        user = new User();
        user.setEmail("refund-test@example.com");
        user.setPassword("secret");
        user.setFullName("Refund Tester");
        user.setPhone("0914000000");
        user = userRepository.save(user);

        Product product = new Product();
        product.setName("San cau long refund");
        product.setPrice(100000);
        product = productRepository.save(product);

        racket = new Racket();
        racket.setName("Yonex Voltric");
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

    private RentalTool rental(RentalToolStatus status, RefundStatus refundStatus) {
        RentalTool rt = new RentalTool();
        rt.setType(RentalType.DAILY);
        rt.setStatus(status);
        rt.setRefundStatus(refundStatus);
        rt.setRacketId(racket.getId());
        rt.setUserId(user.getId());
        rt.setQuantity(1);
        rt.setQuantityDay(1);
        rt.setRentalDate(FUTURE);
        rt.setRentalPrice(60000);
        rt.setFullName("Refund Tester");
        rt.setEmail("refund-test@example.com");
        rt.setPhone("0914000000");
        rt.setCreateAt(LocalDateTime.now());
        rt.setUpdateAt(LocalDateTime.now());
        if (status == RentalToolStatus.CANCELLED) {
            rt.setCancelledAt(LocalDateTime.now());
        }
        return rentalToolRepository.save(rt);
    }

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

    private RefundStatus refundStatusOf(Long id) {
        return rentalToolRepository.findById(id).orElseThrow().getRefundStatus();
    }

    // ------------------------------------------------------------------ confirmRefund

    @Test
    @DisplayName("confirmRefund: CANCELLED + PENDING_REFUND -> REFUNDED + báo user REFUND_DONE")
    void confirmRefund_marksRefundedAndNotifiesUser() {
        RentalTool rt = rental(RentalToolStatus.CANCELLED, RefundStatus.PENDING_REFUND);

        rentalToolService.confirmRefund(rt.getId());

        assertEquals(RefundStatus.REFUNDED, refundStatusOf(rt.getId()));
        verify(notificationService).sendToUser(eq(user.getId()), eq(NotificationType.REFUND_DONE),
                eq("RENTAL_TOOL"), eq(rt.getId()), anyString(), anyString());
    }

    @Test
    @DisplayName("confirmRefund gọi lần hai (đã REFUNDED) -> idempotent, không lỗi")
    void confirmRefund_isIdempotent() {
        RentalTool rt = rental(RentalToolStatus.CANCELLED, RefundStatus.PENDING_REFUND);

        rentalToolService.confirmRefund(rt.getId());
        rentalToolService.confirmRefund(rt.getId());

        assertEquals(RefundStatus.REFUNDED, refundStatusOf(rt.getId()));
        // chỉ báo user đúng 1 lần (lần thứ hai return sớm)
        verify(notificationService, times(1)).sendToUser(eq(user.getId()), eq(NotificationType.REFUND_DONE),
                eq("RENTAL_TOOL"), eq(rt.getId()), anyString(), anyString());
    }

    @Test
    @DisplayName("confirmRefund khi đơn chưa CANCELLED -> IllegalArgumentException")
    void confirmRefund_notCancelled_throws() {
        RentalTool rt = rental(RentalToolStatus.IN_USE, RefundStatus.NOT_APPLICABLE);
        assertThrows(IllegalArgumentException.class, () -> rentalToolService.confirmRefund(rt.getId()));
    }

    @Test
    @DisplayName("confirmRefund khi refundStatus = NOT_APPLICABLE -> IllegalArgumentException")
    void confirmRefund_notApplicable_throws() {
        RentalTool rt = rental(RentalToolStatus.CANCELLED, RefundStatus.NOT_APPLICABLE);
        assertThrows(IllegalArgumentException.class, () -> rentalToolService.confirmRefund(rt.getId()));
    }

    // ------------------------------------------------------------------ admin notify scope

    @Test
    @DisplayName("Huỷ IN_USE -> admin/staff được báo REFUND_REQUEST")
    void cancelInUse_notifiesAdmin() {
        stock(FUTURE, 4, 1, 0);
        RentalTool rt = rental(RentalToolStatus.IN_USE, RefundStatus.NOT_APPLICABLE);

        rentalToolService.cancelRental(rt.getId());

        verify(notificationService, times(1)).sendToAdminStaff(eq(NotificationType.REFUND_REQUEST),
                eq("RENTAL_TOOL"), eq(rt.getId()), anyString(), anyString());
    }

    @Test
    @DisplayName("Huỷ PENDING -> KHÔNG báo admin (chưa thu tiền)")
    void cancelPending_doesNotNotifyAdmin() {
        RentalTool rt = rental(RentalToolStatus.PENDING, RefundStatus.NOT_APPLICABLE);

        rentalToolService.cancelRental(rt.getId());

        verify(notificationService, never()).sendToAdminStaff(eq(NotificationType.REFUND_REQUEST),
                eq("RENTAL_TOOL"), eq(rt.getId()), anyString(), anyString());
    }
}
