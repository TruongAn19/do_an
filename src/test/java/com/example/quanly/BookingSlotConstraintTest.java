package com.example.quanly;

import com.example.quanly.domain.*;
import com.example.quanly.domain.dto.PendingBookingData;
import com.example.quanly.repository.*;
import com.example.quanly.service.BookingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

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
 * A1 — Chống double-booking ở bước confirm.
 *
 * <p>Đây là test JPA/DB thật đầu tiên của dự án: chạy trên H2 (profile "test"),
 * dùng repository thật để kiểm chứng ràng buộc UNIQUE trên {@code booking_detail.active_slot_key}.
 * Hibernate tạo constraint y hệt trong H2 nhờ {@code @UniqueConstraint} khai trên entity
 * {@link BookingDetail}, nên test này phản ánh đúng hành vi production.
 *
 * <p>Lớp KHÔNG đánh {@code @Transactional}: cần commit thật để (a) ràng buộc nổ khi flush và
 * (b) test đồng thời nhìn thấy dữ liệu của nhau. Dọn dữ liệu thủ công ở {@link #cleanup()}.
 */
@SpringBootTest
@ActiveProfiles("test")
class BookingSlotConstraintTest {

    @Autowired BookingService bookingService;
    @Autowired BookingRepository bookingRepository;
    @Autowired BookingDetailRepository bookingDetailRepository;
    @Autowired SubCourtRepository subCourtRepository;
    @Autowired TimeRepository timeRepository;
    @Autowired ProductRepository productRepository;
    @Autowired UserRepository userRepository;
    @Autowired PlatformTransactionManager txManager;

    private User user;
    private Product product;
    private SubCourt subCourt;
    private AvailableTime availableTime;

    private static final LocalDate SLOT_DATE = LocalDate.of(2026, 6, 15);

    @BeforeEach
    void seed() {
        user = new User();
        user.setEmail("a1test@example.com");
        user.setPassword("secret");
        user.setFullName("A1 Tester");
        user.setPhone("0900000000");
        user = userRepository.save(user);

        product = new Product();
        product.setName("San cau long A1");
        product.setPrice(100000);
        product = productRepository.save(product);

        availableTime = new AvailableTime();
        availableTime.setTime(LocalTime.of(18, 0));
        availableTime = timeRepository.save(availableTime);

        subCourt = new SubCourt();
        subCourt.setName("San 1");
        subCourt.setProduct(product);
        subCourt = subCourtRepository.save(subCourt);
    }

    @AfterEach
    void cleanup() {
        // Thứ tự xoá tôn trọng FK: detail -> booking -> sub_court -> product/time -> user
        bookingDetailRepository.deleteAll();
        bookingRepository.deleteAll();
        subCourtRepository.deleteAll();
        timeRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Tạo một Booking ACTIVE đã lưu kèm một BookingDetail chiếm slot SLOT_DATE. */
    private Booking persistActiveBookingOnSlot() {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setAvailableTime(availableTime);
        booking.setBookingDate(SLOT_DATE);
        booking.setBookingType(BookingType.ONE_TIME);
        booking.setStatus(BookingStatus.DA_DAT);
        booking = bookingRepository.save(booking);

        BookingDetail detail = new BookingDetail();
        detail.setBooking(booking);
        detail.setProduct(product);
        detail.setSubCourt(subCourt);
        detail.setAvailableTime(availableTime);
        detail.setDate(SLOT_DATE);
        detail.setActiveSlotKey(slotKey());
        bookingDetailRepository.saveAndFlush(detail);
        return booking;
    }

    private String slotKey() {
        return subCourt.getId() + "-" + availableTime.getId() + "-" + SLOT_DATE;
    }

    /** Dữ liệu confirm cho đúng slot SLOT_DATE (không vợt thuê kèm). */
    private PendingBookingData pendingDataForSlot() {
        List<PendingBookingData.SlotData> slots = new ArrayList<>();
        slots.add(new PendingBookingData.SlotData(SLOT_DATE, 100000, 0));
        return new PendingBookingData(
                new ArrayList<>(),      // temporaryBookingIds (rỗng -> deleteAllHolds no-op)
                user,
                "Nguoi nhan",
                "Dia chi",
                "0900000001",
                product,
                availableTime,
                subCourt,
                SLOT_DATE,
                BookingType.ONE_TIME,
                null,                   // recurringEndDate
                100000,                 // totalBookingPrice
                50000,                  // depositPrice
                slots,
                new ArrayList<>()       // rentals (không có)
        );
    }

    // ------------------------------------------------------------------
    // Test 1: DB từ chối hai slot ACTIVE trùng (sub_court, time, date)
    // ------------------------------------------------------------------
    @Test
    @DisplayName("UNIQUE active_slot_key: insert thứ hai cùng key -> DataIntegrityViolationException")
    void duplicateActiveSlotKey_isRejectedByDb() {
        persistActiveBookingOnSlot(); // detail #1 với key

        Booking booking2 = new Booking();
        booking2.setUser(user);
        booking2.setAvailableTime(availableTime);
        booking2.setBookingDate(SLOT_DATE);
        booking2.setBookingType(BookingType.ONE_TIME);
        booking2.setStatus(BookingStatus.DA_DAT);
        booking2 = bookingRepository.save(booking2);

        BookingDetail dup = new BookingDetail();
        dup.setBooking(booking2);
        dup.setProduct(product);
        dup.setSubCourt(subCourt);
        dup.setAvailableTime(availableTime);
        dup.setDate(SLOT_DATE);
        dup.setActiveSlotKey(slotKey()); // CÙNG key -> phải bị chặn

        final BookingDetail toSave = dup;
        assertThrows(DataIntegrityViolationException.class,
                () -> bookingDetailRepository.saveAndFlush(toSave),
                "DB phải từ chối BookingDetail thứ hai có cùng active_slot_key");
    }

    // ------------------------------------------------------------------
    // Test 2: huỷ (key = NULL) thì slot được đặt lại
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Huỷ booking (active_slot_key=NULL) -> đặt lại slot thành công")
    void cancelledSlot_canBeRebooked() {
        Booking first = persistActiveBookingOnSlot();

        // Giả lập huỷ: xoá key của booking đầu (đúng như BookingService khi set DA_HUY).
        // clearActiveSlotKeyByBooking là @Modifying nên cần transaction -> bọc TransactionTemplate
        // (cũng chính là cách nó chạy trong cancelByUser/updateBooking @Transactional).
        first.setStatus(BookingStatus.DA_HUY);
        bookingRepository.save(first);
        final long firstId = first.getId();
        new TransactionTemplate(txManager).executeWithoutResult(
                s -> bookingDetailRepository.clearActiveSlotKeyByBooking(firstId));

        // Đặt lại cùng slot -> KHÔNG được ném exception (key cũ đã NULL)
        Booking second = new Booking();
        second.setUser(user);
        second.setAvailableTime(availableTime);
        second.setBookingDate(SLOT_DATE);
        second.setBookingType(BookingType.ONE_TIME);
        second.setStatus(BookingStatus.DA_DAT);
        second = bookingRepository.save(second);

        BookingDetail rebook = new BookingDetail();
        rebook.setBooking(second);
        rebook.setProduct(product);
        rebook.setSubCourt(subCourt);
        rebook.setAvailableTime(availableTime);
        rebook.setDate(SLOT_DATE);
        rebook.setActiveSlotKey(slotKey());

        final BookingDetail toSave = rebook;
        assertDoesNotThrow(() -> bookingDetailRepository.saveAndFlush(toSave),
                "Slot của booking đã huỷ phải cho phép đặt lại");
    }

    // ------------------------------------------------------------------
    // Test 3: confirmPendingBooking khi slot đã bị chiếm -> IllegalStateException (→ 409)
    // ------------------------------------------------------------------
    @Test
    @DisplayName("confirmPendingBooking trên slot đã có booking active -> IllegalStateException")
    void confirmOnTakenSlot_throwsIllegalState() {
        persistActiveBookingOnSlot();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> bookingService.confirmPendingBooking(pendingDataForSlot()));
        assertTrue(ex.getMessage().contains("Sân đã bị đặt bởi người khác trong lúc thanh toán"),
                "Message phải giữ đúng format để PaymentController trả 409");
        assertTrue(ex.getMessage().contains("15/06/2026"), "Message nên kèm ngày dd/MM/yyyy");
    }

    // ------------------------------------------------------------------
    // Test 4: hai confirm đồng thời cùng slot -> đúng một booking thành công
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Hai confirm đồng thời cùng slot -> 1 thành công, 1 nhận 409; chỉ 1 booking_detail")
    void concurrentConfirm_onlyOneSucceeds() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2); // đảm bảo hai luồng vào confirm gần như cùng lúc

        Callable<String> task = () -> {
            barrier.await();
            try {
                bookingService.confirmPendingBooking(pendingDataForSlot());
                return "OK";
            } catch (IllegalStateException e) {
                return "CONFLICT"; // dù chặn bởi SELECT sớm hay UNIQUE của DB đều ra đây
            }
        };

        try {
            Future<String> f1 = pool.submit(task);
            Future<String> f2 = pool.submit(task);
            List<String> results = List.of(f1.get(), f2.get());

            long ok = results.stream().filter("OK"::equals).count();
            long conflict = results.stream().filter("CONFLICT"::equals).count();
            assertEquals(1, ok, "Đúng một luồng được tạo booking");
            assertEquals(1, conflict, "Luồng còn lại phải nhận xung đột (409)");

            // Chỉ tồn tại đúng 1 BookingDetail (1 booking thành công, không trùng lịch)
            assertEquals(1, bookingDetailRepository.count(),
                    "Phải chỉ có duy nhất một booking_detail active cho slot");
        } finally {
            pool.shutdownNow();
        }
    }
}
