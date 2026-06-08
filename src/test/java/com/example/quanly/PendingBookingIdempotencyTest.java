package com.example.quanly;

import com.example.quanly.controller.PaymentController;
import com.example.quanly.domain.*;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.domain.dto.PendingBookingData;
import com.example.quanly.repository.*;
import com.example.quanly.service.NotificationService;
import com.example.quanly.service.PaymentService;
import com.example.quanly.service.PendingBookingCache;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * A3 — Làm callback VNPay idempotent dưới điều kiện đồng thời.
 *
 * <p>Cùng pattern với {@link RentalStockConfirmTest}: H2 thật (profile "test"), KHÔNG
 * {@code @Transactional} (cần commit thật để confirm/getAndRemove có hiệu lực giữa các luồng),
 * dọn dữ liệu thủ công. {@link PaymentService} bị mock để bỏ qua kiểm tra chữ ký VNPay; cache
 * Redis là fake in-memory có trạng thái (xem {@code TestRedisConfiguration}).
 */
@SpringBootTest
@ActiveProfiles("test")
class PendingBookingIdempotencyTest {

    @Autowired PaymentController paymentController;
    @Autowired PendingBookingCache pendingBookingCache;
    @Autowired BookingRepository bookingRepository;
    @Autowired BookingDetailRepository bookingDetailRepository;
    @Autowired RentalToolRepository rentalToolRepository;
    @Autowired RacketRepository racketRepository;
    @Autowired SubCourtRepository subCourtRepository;
    @Autowired TimeRepository timeRepository;
    @Autowired ProductRepository productRepository;
    @Autowired UserRepository userRepository;

    @MockBean PaymentService paymentService;       // bỏ qua verify chữ ký
    @SpyBean NotificationService notificationService; // verify REFUND_REQUEST khi confirm fail

    private User user;
    private Product product;
    private SubCourt subCourt;
    private AvailableTime availableTime;

    private static final LocalDate SLOT_DATE = LocalDate.of(2026, 8, 12);

    @BeforeEach
    void seed() {
        // Mọi callback đều coi như chữ ký hợp lệ.
        Mockito.when(paymentService.verifyVnpayCallback(any())).thenReturn(true);

        user = new User();
        user.setEmail("a3test@example.com");
        user.setPassword("secret");
        user.setFullName("A3 Tester");
        user.setPhone("0911111111");
        user = userRepository.save(user);

        product = new Product();
        product.setName("San cau long A3");
        product.setPrice(100000);
        product = productRepository.save(product);

        availableTime = new AvailableTime();
        availableTime.setTime(LocalTime.of(20, 0));
        availableTime = timeRepository.save(availableTime);

        subCourt = new SubCourt();
        subCourt.setName("San A3");
        subCourt.setProduct(product);
        subCourt = subCourtRepository.save(subCourt);
    }

    @AfterEach
    void cleanup() {
        bookingDetailRepository.deleteAll();
        bookingRepository.deleteAll();
        rentalToolRepository.deleteAll();
        racketRepository.deleteAll();
        subCourtRepository.deleteAll();
        timeRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------
    private PendingBookingData pendingData(LocalDate date) {
        List<PendingBookingData.SlotData> slots = new ArrayList<>();
        slots.add(new PendingBookingData.SlotData(date, 100000, 0));
        return new PendingBookingData(
                new ArrayList<>(), user, "Nguoi nhan", "Dia chi", "0911111112",
                product, availableTime, subCourt,
                date, BookingType.ONE_TIME, null,
                100000, 50000,
                slots, new ArrayList<>());
    }

    /** Mock một HttpServletRequest callback cho pendingId với mã phản hồi cho trước. */
    private HttpServletRequest callbackRequest(long pendingId, String responseCode) {
        String amountStr = null;
        // Dùng get để không phá huỷ snapshot trong Redis cache
        java.util.Optional<PendingBookingData> dataOpt = pendingBookingCache.get(pendingId);
        if (dataOpt.isPresent()) {
            amountStr = String.valueOf((long) (dataOpt.get().getDepositPrice() * 100));
        }
        return callbackRequest(pendingId, responseCode, amountStr);
    }

    private HttpServletRequest callbackRequest(long pendingId, String responseCode, String vnpAmount) {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getParameter("vnp_ResponseCode")).thenReturn(responseCode);
        Mockito.when(req.getParameter("vnp_OrderInfo")).thenReturn(pendingId + "-PENDING_BOOKING");
        Mockito.when(req.getParameter("vnp_Amount")).thenReturn(vnpAmount);
        return req;
    }

    private Map<String, Object> dataOf(ResponseEntity<ApiResponse<Map<String, Object>>> resp) {
        return resp.getBody() == null ? null : resp.getBody().getData();
    }

    // ------------------------------------------------------------------
    // Test 1 (bắt buộc): callback success hai lần cùng pendingId -> chỉ 1 booking
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Callback success 2 lần cùng pendingId -> chỉ tạo 1 booking; lần 2 báo đã xác nhận trước đó")
    void duplicateSuccessCallback_createsOneBooking() {
        long pendingId = pendingBookingCache.store(pendingData(SLOT_DATE));

        ResponseEntity<ApiResponse<Map<String, Object>>> first =
                paymentController.handleVnpayCallback(callbackRequest(pendingId, "00"));
        ResponseEntity<ApiResponse<Map<String, Object>>> second =
                paymentController.handleVnpayCallback(callbackRequest(pendingId, "00"));

        assertEquals(200, first.getStatusCode().value());
        assertNotNull(dataOf(first).get("bookingId"), "Lần đầu phải tạo booking");

        assertEquals(200, second.getStatusCode().value(), "Callback trùng vẫn 200 (không phải lỗi)");
        assertEquals("Đơn đã được xác nhận trước đó", second.getBody().getMessage());
        assertEquals(dataOf(first).get("bookingCode"), dataOf(second).get("bookingCode"),
                "Lần 2 trả về đúng bookingCode đã tạo");

        assertEquals(1, bookingRepository.count(), "Một lần thanh toán chỉ được tạo đúng 1 booking");
    }

    // ------------------------------------------------------------------
    // Test 2: hai callback success đồng thời cùng pendingId -> vẫn chỉ 1 booking
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Hai callback success đồng thời cùng pendingId -> đúng 1 booking")
    void concurrentSuccessCallback_createsOneBooking() throws Exception {
        long pendingId = pendingBookingCache.store(pendingData(SLOT_DATE));

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2);
        Callable<Integer> task = () -> {
            barrier.await();
            return paymentController.handleVnpayCallback(callbackRequest(pendingId, "00"))
                    .getStatusCode().value();
        };
        try {
            Future<Integer> f1 = pool.submit(task);
            Future<Integer> f2 = pool.submit(task);
            f1.get();
            f2.get();
            assertEquals(1, bookingRepository.count(), "Hai callback đồng thời chỉ tạo 1 booking");
        } finally {
            pool.shutdownNow();
        }
    }

    // ------------------------------------------------------------------
    // Test 3: pendingId không tồn tại thật (chưa từng store) -> 400, không marker
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Callback với pendingId không tồn tại/hết hạn -> 400")
    void unknownPendingId_returns400() {
        long unknownId = ThreadLocalRandom.current().nextLong(1_000_000L, Long.MAX_VALUE);
        ResponseEntity<ApiResponse<Map<String, Object>>> resp =
                paymentController.handleVnpayCallback(callbackRequest(unknownId, "00"));

        assertEquals(400, resp.getStatusCode().value());
        assertEquals("Phiên đặt sân không tồn tại hoặc đã hết hạn", resp.getBody().getMessage());
        assertEquals(0, bookingRepository.count());
    }

    // ------------------------------------------------------------------
    // Test 4: thanh toán THẤT BẠI hai lần -> không double-release, không booking
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Callback thất bại 2 lần -> giải phóng giữ chỗ đúng 1 lần, không tạo booking")
    void duplicateFailedCallback_releasesOnce() {
        long pendingId = pendingBookingCache.store(pendingData(SLOT_DATE));

        ResponseEntity<ApiResponse<Map<String, Object>>> first =
                paymentController.handleVnpayCallback(callbackRequest(pendingId, "07")); // != "00"
        ResponseEntity<ApiResponse<Map<String, Object>>> second =
                paymentController.handleVnpayCallback(callbackRequest(pendingId, "07"));

        assertEquals(200, first.getStatusCode().value());
        assertEquals("FAILED", dataOf(first).get("status"));

        // Lần 2: snapshot đã bị consume, không có marker -> 400 (không cancel lần hai)
        assertEquals(400, second.getStatusCode().value());
        assertEquals(0, bookingRepository.count());
    }

    // ------------------------------------------------------------------
    // Test 5 (A3.2): confirm fail sau khi đã thu tiền -> 409 + flag hoàn tiền thủ công
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Confirm fail sau thu tiền -> 409 + gửi REFUND_REQUEST cho admin/staff")
    void confirmFailsAfterPayment_flagsManualRefund() {
        // Tạo trước một booking active chiếm đúng slot/ngày để confirm lần sau bị xung đột (A1).
        long firstId = pendingBookingCache.store(pendingData(SLOT_DATE));
        paymentController.handleVnpayCallback(callbackRequest(firstId, "00"));
        assertEquals(1, bookingRepository.count());

        // Callback thứ hai cho slot trùng -> confirmPendingBooking ném IllegalStateException.
        long conflictId = pendingBookingCache.store(pendingData(SLOT_DATE));
        ResponseEntity<ApiResponse<Map<String, Object>>> resp =
                paymentController.handleVnpayCallback(callbackRequest(conflictId, "00"));

        assertEquals(409, resp.getStatusCode().value(), "Xung đột sau thanh toán -> 409");
        assertEquals(1, bookingRepository.count(), "Không tạo thêm booking nửa vời");

        // Phải đánh dấu cần hoàn tiền thủ công cho admin/staff.
        Mockito.verify(notificationService).sendToAdminStaff(
                eq(NotificationType.REFUND_REQUEST), eq("PENDING_BOOKING"), eq(conflictId),
                anyString(), anyString());

        // Snapshot đã bị consume, không marker -> callback lại nhận 400 (không retry âm thầm).
        ResponseEntity<ApiResponse<Map<String, Object>>> retry =
                paymentController.handleVnpayCallback(callbackRequest(conflictId, "00"));
        assertEquals(400, retry.getStatusCode().value());
    }

    // ------------------------------------------------------------------
    // Test 6 (A4): callback với vnp_Amount sai cho PENDING_BOOKING -> 400, giải phóng hold, notify admin
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Callback với vnp_Amount sai cho PENDING_BOOKING -> bị từ chối (400), giải phóng hold, gửi REFUND_REQUEST")
    void callbackWithWrongAmount_rejectsAndReleasesHold() {
        long pendingId = pendingBookingCache.store(pendingData(SLOT_DATE));
        // Mong đợi 50000 * 100 = 5000000, truyền sai là 4900000
        HttpServletRequest req = callbackRequest(pendingId, "00", "4900000");

        ResponseEntity<ApiResponse<Map<String, Object>>> resp = paymentController.handleVnpayCallback(req);

        assertEquals(400, resp.getStatusCode().value());
        assertEquals("Số tiền thanh toán không khớp", resp.getBody().getMessage());
        assertEquals(0, bookingRepository.count(), "Không được tạo booking");

        // Cache snapshot bị giải phóng/getAndRemove xoá khỏi Redis
        assertFalse(pendingBookingCache.get(pendingId).isPresent());

        // Phải gửi yêu cầu hoàn tiền thủ công cho admin/staff
        Mockito.verify(notificationService).sendToAdminStaff(
                eq(NotificationType.REFUND_REQUEST), eq("PENDING_BOOKING"), eq(pendingId),
                anyString(), anyString());
    }

    // ------------------------------------------------------------------
    // Test 7 (A4): callback với vnp_Amount sai cho RENTAL_TOOL -> 400, chuyển CANCELLED, notify admin
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Callback với vnp_Amount sai cho RENTAL_TOOL -> bị từ chối (400), chuyển CANCELLED, gửi REFUND_REQUEST")
    void callbackWithWrongAmount_rentalTool_rejectsAndCancels() {
        RentalTool rt = new RentalTool();
        rt.setFullName("Rental Tester");
        rt.setEmail("rt@example.com");
        rt.setPhone("0900000000");
        rt.setType(RentalType.DAILY);
        rt.setPrice(150000.0);
        rt.setStatus(RentalToolStatus.PENDING);
        rt.setQuantity(1);
        rt.setQuantityDay(1);
        rt.setRentalDate(SLOT_DATE);
        rt.setUserId(user.getId());
        rt = rentalToolRepository.save(rt);

        // Mock callback request
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getParameter("vnp_ResponseCode")).thenReturn("00");
        Mockito.when(req.getParameter("vnp_OrderInfo")).thenReturn(rt.getId() + "-RENTAL_TOOL");
        // Mong đợi 150000 * 100 = 15000000, truyền sai là 14000000
        Mockito.when(req.getParameter("vnp_Amount")).thenReturn("14000000");

        ResponseEntity<ApiResponse<Map<String, Object>>> resp = paymentController.handleVnpayCallback(req);

        assertEquals(400, resp.getStatusCode().value());
        assertEquals("Số tiền thanh toán không khớp", resp.getBody().getMessage());

        RentalTool after = rentalToolRepository.findById(rt.getId()).orElseThrow();
        assertEquals(RentalToolStatus.CANCELLED, after.getStatus());

        // Phải gửi yêu cầu hoàn tiền thủ công cho admin/staff
        Mockito.verify(notificationService).sendToAdminStaff(
                eq(NotificationType.REFUND_REQUEST), eq("RENTAL_TOOL"), eq(rt.getId()),
                anyString(), anyString());
    }
}
