package com.example.quanly;

import com.example.quanly.config.HoldPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A6 — Cặp đôi với {@link HoldPolicyDuration7Test}. Xem javadoc ở đó để hiểu phép chứng minh
 * "không còn rò rỉ static". Class này nạp context riêng với {@code booking.hold.duration-minutes=11}.
 */
@SpringBootTest(properties = "booking.hold.duration-minutes=11")
@ActiveProfiles("test")
class HoldPolicyDuration11Test {

    @Autowired HoldPolicy holdPolicy;

    @Test
    @DisplayName("Context duration=11 thấy đúng 11, không bị context khác (5/7) ghi đè")
    void seesOwnConfiguredValue() {
        assertEquals(11, holdPolicy.getDurationMinutes());
    }
}
