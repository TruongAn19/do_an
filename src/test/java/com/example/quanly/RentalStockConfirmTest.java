package com.example.quanly;

import com.example.quanly.domain.*;
import com.example.quanly.domain.dto.BookingResponseDTO;
import com.example.quanly.domain.dto.PendingBookingData;
import com.example.quanly.repository.*;
import com.example.quanly.service.BookingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A2 — Re-check + lock tồn kho vợt thuê kèm lúc confirm.
 *
 * <p>Cùng pattern với {@link BookingSlotConstraintTest}: chạy trên H2 thật (profile "test"),
 * KHÔNG {@code @Transactional} (cần commit thật để rollback của confirm và lock bi quan có hiệu lực
 * giữa các luồng), dọn dữ liệu thủ công ở {@link #cleanup()}.
 */
@SpringBootTest
@ActiveProfiles("test")
class RentalStockConfirmTest {

    @Autowired BookingService bookingService;
    @Autowired BookingRepository bookingRepository;
    @Autowired BookingDetailRepository bookingDetailRepository;
    @Autowired RentalToolRepository rentalToolRepository;
    @Autowired RacketRepository racketRepository;
    @Autowired SubCourtRepository subCourtRepository;
    @Autowired TimeRepository timeRepository;
    @Autowired ProductRepository productRepository;
    @Autowired UserRepository userRepository;

    private User user;
    private Product product;
    private SubCourt subCourt;
    private AvailableTime availableTime;
    private Racket racket;

    private static final LocalDate SLOT_DATE = LocalDate.of(2026, 7, 10);

    @BeforeEach
    void seed() {
        user = new User();
        user.setEmail("a2test@example.com");
        user.setPassword("secret");
        user.setFullName("A2 Tester");
        user.setPhone("0900000000");
        user = userRepository.save(user);

        product = new Product();
        product.setName("San cau long A2");
        product.setPrice(100000);
        product = productRepository.save(product);

        availableTime = new AvailableTime();
        availableTime.setTime(LocalTime.of(19, 0));
        availableTime = timeRepository.save(availableTime);

        subCourt = new SubCourt();
        subCourt.setName("San 1");
        subCourt.setProduct(product);
        subCourt = subCourtRepository.save(subCourt);

        racket = new Racket();
        racket.setName("Yonex Astrox");
        racket.setPrice(500000);
        racket.setRentalPricePerPlay(30000);
        racket.setAvailable(true);
        racket.setProduct(product);
        racket.setBookingStockQuantity(1); // mặc định: chỉ 1 chiếc
        racket = racketRepository.save(racket);
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
    // Helper: PendingBookingData cho slot (date) thuê `qty` chiếc của racket hiện tại.
    // ------------------------------------------------------------------
    private PendingBookingData pendingData(LocalDate date, int qty) {
        List<PendingBookingData.SlotData> slots = new ArrayList<>();
        slots.add(new PendingBookingData.SlotData(date, 100000, 0));

        List<PendingBookingData.RentalSlot> rentals = new ArrayList<>();
        rentals.add(new PendingBookingData.RentalSlot(
                racket, qty, racket.getRentalPricePerPlay(), racket.getRentalPricePerPlay() * qty));

        return new PendingBookingData(
                new ArrayList<>(),      // temporaryBookingIds
                user, "Nguoi nhan", "Dia chi", "0900000001",
                product, availableTime, subCourt,
                date, BookingType.ONE_TIME, null,
                100000 + racket.getRentalPricePerPlay() * qty, // totalBookingPrice
                50000 + racket.getRentalPricePerPlay() * qty,  // depositPrice
                slots, rentals);
    }

    // ------------------------------------------------------------------
    // Test 1 (bắt buộc): thiếu tồn kho -> IllegalStateException, rollback sạch, stock không âm
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Confirm khi vợt không đủ tồn -> 409 + rollback, không tạo booking, stock không âm")
    void confirmWithInsufficientStock_rollsBack() {
        // stock = 1 nhưng yêu cầu 2
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> bookingService.confirmPendingBooking(pendingData(SLOT_DATE, 2)));
        assertTrue(ex.getMessage().contains("đã hết hàng trong lúc thanh toán"),
                "Message phải đúng format để controller trả 409");

        // Rollback toàn bộ: không booking, không detail, không rental tool
        assertEquals(0, bookingRepository.count(), "Không được tạo booking nửa vời");
        assertEquals(0, bookingDetailRepository.count(), "Không được tạo booking_detail");
        assertEquals(0, rentalToolRepository.count(), "Không được tạo rental tool");

        // Stock giữ nguyên, không âm
        Racket after = racketRepository.findById(racket.getId()).orElseThrow();
        assertEquals(1, after.getBookingStockQuantity(), "Stock phải giữ nguyên, không bị trừ/âm");
    }

    // ------------------------------------------------------------------
    // Test 2: đủ tồn -> confirm thành công, trừ stock đúng, có RentalTool IN_USE
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Confirm khi đủ tồn -> thành công, trừ stock đúng, tạo RentalTool IN_USE")
    void confirmWithEnoughStock_succeeds() {
        BookingResponseDTO dto = bookingService.confirmPendingBooking(pendingData(SLOT_DATE, 1));
        assertNotNull(dto);

        assertEquals(1, bookingRepository.count());
        assertEquals(1, rentalToolRepository.count());
        assertEquals(RentalToolStatus.IN_USE, rentalToolRepository.findAll().get(0).getStatus());

        Racket after = racketRepository.findById(racket.getId()).orElseThrow();
        assertEquals(0, after.getBookingStockQuantity(), "Stock phải giảm đúng 1");
    }

    // ------------------------------------------------------------------
    // Test 3 (optional): hai confirm đồng thời cùng vợt (slot khác nhau) -> 1 OK, 1 hết hàng
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Hai confirm đồng thời cùng vợt (stock=1) -> 1 thành công, 1 nhận hết hàng; stock=0")
    void concurrentConfirm_doesNotOverbookRacket() throws Exception {
        // Hai slot KHÁC NGÀY để không vướng UNIQUE slot của A1; cùng dùng racket stock=1.
        LocalDate dateA = SLOT_DATE;
        LocalDate dateB = SLOT_DATE.plusDays(1);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2);

        Callable<String> taskA = () -> runConfirm(barrier, dateA);
        Callable<String> taskB = () -> runConfirm(barrier, dateB);

        try {
            Future<String> f1 = pool.submit(taskA);
            Future<String> f2 = pool.submit(taskB);
            List<String> results = List.of(f1.get(), f2.get());

            assertEquals(1, results.stream().filter("OK"::equals).count(), "Đúng một confirm thành công");
            assertEquals(1, results.stream().filter("OUT_OF_STOCK"::equals).count(),
                    "Confirm còn lại phải báo hết hàng");

            Racket after = racketRepository.findById(racket.getId()).orElseThrow();
            assertEquals(0, after.getBookingStockQuantity(), "Stock cuối = 0, không overbook (không âm)");
            assertEquals(1, rentalToolRepository.count(), "Chỉ một RentalTool được tạo");
        } finally {
            pool.shutdownNow();
        }
    }

    private String runConfirm(CyclicBarrier barrier, LocalDate date) throws Exception {
        barrier.await();
        try {
            bookingService.confirmPendingBooking(pendingData(date, 1));
            return "OK";
        } catch (IllegalStateException e) {
            return "OUT_OF_STOCK";
        }
    }
}
