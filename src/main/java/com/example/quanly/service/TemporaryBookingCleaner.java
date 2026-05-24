package com.example.quanly.service;

import com.example.quanly.domain.TemporaryBooking;
import com.example.quanly.repository.TemporaryBookingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TemporaryBookingCleaner {

    /** Phải khớp với {@link TemporaryBooking#isExpired()} — buffer 3 phút sau holdStartTime. */
    private static final int HOLD_EXPIRY_MINUTES = 3;

    private final TemporaryBookingRepository temporaryBookingRepository;
    private final SlotEventPublisher slotEventPublisher;

    @Scheduled(fixedRate = 60000) // chạy mỗi 60 giây
    @Transactional
    public void cleanExpiredHolds() {
        // Chỉ load các hold đã expired (không phải cả bảng) — quan trọng khi WEEKLY_RECURRING
        // có thể sinh nhiều hold cùng lúc. Query JOIN FETCH subCourt/availableTime để event
        // publish bên dưới không trigger N+1.
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(HOLD_EXPIRY_MINUTES);
        List<TemporaryBooking> expired = temporaryBookingRepository.findExpiredHoldsForCleanup(cutoff);
        if (expired.isEmpty()) {
            return;
        }

        for (TemporaryBooking tb : expired) {
            Long subCourtId = tb.getSubCourt() != null ? tb.getSubCourt().getId() : null;
            Long availableTimeId = tb.getAvailableTime() != null ? tb.getAvailableTime().getId() : null;
            if (subCourtId != null && availableTimeId != null && tb.getBookingDate() != null) {
                slotEventPublisher.publishReleased(subCourtId, availableTimeId, tb.getBookingDate());
            }
        }

        // Bulk delete by id IN (...) — 1 statement thay vì N delete riêng lẻ.
        temporaryBookingRepository.deleteAllInBatch(expired);
    }
}
