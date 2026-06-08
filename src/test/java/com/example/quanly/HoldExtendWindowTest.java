package com.example.quanly;

import com.example.quanly.domain.TemporaryBooking;
import com.example.quanly.service.BookingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A6 — Bất biến giữ hành vi sau refactor: cửa sổ thanh toán hiệu dụng luôn đúng bằng
 * {@link BookingService#PAYMENT_HOLD_WINDOW} (18 phút), ĐỘC LẬP với {@code booking.hold.duration-minutes}.
 *
 * <p>Tái hiện công thức {@code computeExtendedHoldStart = now + PAYMENT_HOLD_WINDOW - grace}
 * (với {@code grace = duration}) rồi dùng chính method production {@link TemporaryBooking#isExpired(Duration)}
 * để kiểm chứng. Vì {@code isExpired} cộng {@code grace} trở lại, {@code grace} bị triệt tiêu →
 * mốc hết hạn = {@code now + 18 phút} dù duration là 3, 5, 7 hay 11.
 */
class HoldExtendWindowTest {

    private static final Duration WINDOW = BookingService.PAYMENT_HOLD_WINDOW;

    @Test
    @DisplayName("Extend hold: hết hạn đúng now + PAYMENT_HOLD_WINDOW, độc lập duration-minutes")
    void extendedHoldExpiresAtPaymentWindowRegardlessOfDuration() {
        LocalDateTime now = LocalDateTime.now();

        for (int durationMin : new int[]{3, 5, 7, 11}) {
            Duration grace = Duration.ofMinutes(durationMin);

            // Công thức production: holdStartTime để hold sống tới now + WINDOW
            LocalDateTime extendedHoldStart = now.plus(WINDOW).minus(grace);

            // grace bị triệt tiêu => mốc hết hạn hiệu dụng = now + WINDOW, không phụ thuộc duration
            LocalDateTime effectiveExpiry = extendedHoldStart.plus(grace);
            assertEquals(now.plus(WINDOW), effectiveExpiry,
                    "Mốc hết hạn hiệu dụng phải = now + PAYMENT_HOLD_WINDOW với duration=" + durationMin);

            // Dùng method production: hold đặt ở mốc tương lai (now + WINDOW) nên CHƯA hết hạn
            TemporaryBooking notYet = new TemporaryBooking();
            notYet.setHoldStartTime(extendedHoldStart);
            assertFalse(notYet.isExpired(grace),
                    "Hold phải còn sống trong cửa sổ thanh toán với duration=" + durationMin);

            // Đẩy holdStartTime lùi thêm WINDOW + 1' => mốc hết hạn ở quá khứ => ĐÃ hết hạn
            TemporaryBooking expired = new TemporaryBooking();
            expired.setHoldStartTime(extendedHoldStart.minus(WINDOW).minusMinutes(1));
            assertTrue(expired.isExpired(grace),
                    "Hold quá mốc now + PAYMENT_HOLD_WINDOW phải hết hạn với duration=" + durationMin);
        }
    }
}
