package com.pitchbooking.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Cấu hình thời gian giữ chỗ (hold) cho TemporaryBooking, inject như một bean
 * Spring thật.
 *
 * <p>
 * Thay thế anti-pattern cũ: field {@code static} mutable trên entity
 * {@code TemporaryBooking}
 * được set qua {@code @PostConstruct}. State cấu hình toàn cục nằm trong entity
 * JPA gây rò rỉ
 * giá trị giữa các test trong cùng JVM và phụ thuộc thứ tự khởi tạo Spring
 * context.
 */
@Component
public class HoldPolicy {

    private final int durationMinutes;

    public HoldPolicy(@Value("${booking.hold.duration-minutes:3}") int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public Duration getHoldDuration() {
        return Duration.ofMinutes(durationMinutes);
    }
}
