package com.example.quanly;

import com.example.quanly.config.HoldPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A6 — Chứng minh đã loại bỏ mutable static khỏi entity.
 *
 * <p>Class này chạy với {@code booking.hold.duration-minutes=7}, còn {@link HoldPolicyDuration11Test}
 * chạy với {@code =11} (và {@link BookingHoldExpiryTest} với {@code =5}) trong CÙNG JVM của lần
 * {@code mvnw test}. Mỗi {@code @SpringBootTest} property set khác nhau nạp một ApplicationContext
 * riêng, nên mỗi context có một bean {@link HoldPolicy} độc lập.
 *
 * <p>Với anti-pattern cũ (field {@code static} trên {@code TemporaryBooking} set qua
 * {@code @PostConstruct}), context khởi tạo sau cùng sẽ GHI ĐÈ giá trị static → mọi class thấy
 * chung một giá trị và ít nhất một trong các assert này sẽ fail. Cả ba cùng xanh = không còn rò rỉ.
 */
@SpringBootTest(properties = "booking.hold.duration-minutes=7")
@ActiveProfiles("test")
class HoldPolicyDuration7Test {

    @Autowired HoldPolicy holdPolicy;

    @Test
    @DisplayName("Context duration=7 thấy đúng 7, không bị context khác (5/11) ghi đè")
    void seesOwnConfiguredValue() {
        assertEquals(7, holdPolicy.getDurationMinutes());
    }
}
