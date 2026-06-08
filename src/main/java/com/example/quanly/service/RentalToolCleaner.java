package com.example.quanly.service;

import com.example.quanly.domain.RentalTool;
import com.example.quanly.domain.RentalToolStatus;
import com.example.quanly.domain.RentalType;
import com.example.quanly.repository.RentalToolRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tự huỷ các đơn thuê vợt standalone (DAILY) ở trạng thái PENDING quá hạn thanh toán (B3).
 *
 * <p>Style giống {@code TemporaryBookingCleaner}. Tách riêng khỏi {@link RentalToolService} để mỗi
 * lần huỷ đi qua proxy Spring → {@link RentalToolService#cancelRental(Long)} chạy trong transaction
 * RIÊNG: một đơn lỗi không rollback cả lô. Vì đơn PENDING chưa trừ kho nên cancelRental chỉ đổi
 * trạng thái + notify user (không đụng stock).
 */
@Slf4j
@Component
public class RentalToolCleaner {

    private final RentalToolRepository rentalToolRepository;
    private final RentalToolService rentalToolService;
    private final int expiryMinutes;

    public RentalToolCleaner(RentalToolRepository rentalToolRepository,
                             RentalToolService rentalToolService,
                             @Value("${rental.pending.expiry-minutes:30}") int expiryMinutes) {
        this.rentalToolRepository = rentalToolRepository;
        this.rentalToolService = rentalToolService;
        this.expiryMinutes = expiryMinutes;
    }

    @Scheduled(fixedRate = 60000) // chạy mỗi 60 giây
    public void expirePendingRentals() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(expiryMinutes);
        List<RentalTool> expired = rentalToolRepository
                .findByStatusAndTypeAndCreateAtBefore(RentalToolStatus.PENDING, RentalType.DAILY, threshold);
        if (expired.isEmpty()) {
            return;
        }

        for (RentalTool rt : expired) {
            try {
                // PENDING → cancelRental chỉ đổi trạng thái, KHÔNG đụng stock; có notify user.
                rentalToolService.cancelRental(rt.getId());
            } catch (Exception ex) {
                log.warn("Tự huỷ đơn thuê PENDING quá hạn thất bại id={}: {}", rt.getId(), ex.getMessage());
            }
        }
        log.info("Đã tự huỷ {} đơn thuê PENDING quá hạn (>{} phút).", expired.size(), expiryMinutes);
    }
}
