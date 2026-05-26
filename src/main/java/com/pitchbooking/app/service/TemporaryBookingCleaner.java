package com.pitchbooking.app.service;

import com.pitchbooking.app.domain.TemporaryBooking;
import com.pitchbooking.app.repository.TemporaryBookingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TemporaryBookingCleaner {
    private final TemporaryBookingRepository temporaryBookingRepository;

    public TemporaryBookingCleaner(TemporaryBookingRepository temporaryBookingRepository) {
        this.temporaryBookingRepository = temporaryBookingRepository;
    }

    @Scheduled(fixedRate = 60000) // chạy mỗi 60 giây
    public void cleanExpiredHolds() {
        List<TemporaryBooking> all = temporaryBookingRepository.findAll();
        for (TemporaryBooking tb : all) {
            if (tb.isExpired()) {
                temporaryBookingRepository.delete(tb);
            }
        }
    }
}
