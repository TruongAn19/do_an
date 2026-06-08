package com.example.quanly.service;

import com.example.quanly.domain.Racket;
import com.example.quanly.domain.RacketStockByDate;
import com.example.quanly.domain.dto.CheckStockRequest;
import com.example.quanly.exception.ResourceNotFoundException;
import com.example.quanly.repository.RacketRepository;
import com.example.quanly.repository.RacketStockByDateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@EnableScheduling
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RacketStockByDateService {

    RacketRepository racketRepository;
    RacketStockByDateRepository racketStockByDateRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void generateStockByDate() {
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusDays(6);
        List<Racket> allRackets = racketRepository.findAll();
        for (Racket racket : allRackets) {
            for (LocalDate date = today; !date.isAfter(targetDate); date = date.plusDays(1)) {
                boolean exists = racketStockByDateRepository.existsByRacketIdAndDate(racket.getId(), date);
                if (!exists) {
                    RacketStockByDate stock = new RacketStockByDate();
                    stock.setRacketId(racket.getId());
                    stock.setDate(date);
                    stock.setTotalStock(racket.getQuantity());
                    stock.setAvailableStock(racket.getQuantity());
                    stock.setReservedStock(0);
                    stock.setRentalStock(0);
                    racketStockByDateRepository.save(stock);
                }
            }
        }
    }

    @Async
    public void generateStockForRacket(Racket racket) {
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusDays(6);

        List<RacketStockByDate> stocks = new ArrayList<>();

        for (LocalDate date = today; !date.isAfter(targetDate); date = date.plusDays(1)) {
            RacketStockByDate stock = new RacketStockByDate();
            stock.setRacketId(racket.getId());
            stock.setDate(date);
            stock.setTotalStock(racket.getQuantity());
            stock.setAvailableStock(racket.getQuantity());
            stock.setReservedStock(0);
            stock.setRentalStock(0);
            stocks.add(stock);
        }

        racketStockByDateRepository.saveAll(stocks);
    }

    /**
     * Trả tồn kho theo ngày cho 1 vợt. Nếu chưa có row cho ngày đó (vd job
     * {@link #generateStockByDate()} chưa chạy tới ngày này, hoặc người dùng chọn ngày ngoài cửa sổ
     * 7 ngày), TẠO MỚI on-demand từ {@code racket.quantity} thay vì trả {@code null} — tránh hiển thị
     * "hết hàng" giả khi thực ra chưa có ai thuê ngày đó.
     */
    @Transactional
    public RacketStockByDate getStock(CheckStockRequest request) {
        return racketStockByDateRepository
                .findByRacketIdAndDate(request.getRacketId(), request.getDate())
                .orElseGet(() -> createDefaultStock(request.getRacketId(), request.getDate()));
    }

    /** Khởi tạo row tồn kho mặc định cho (racket, date): khả dụng = tổng = racket.quantity. */
    private RacketStockByDate createDefaultStock(Long racketId, LocalDate date) {
        Racket racket = racketRepository.findById(racketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vợt id=" + racketId));
        RacketStockByDate stock = new RacketStockByDate();
        stock.setRacketId(racketId);
        stock.setDate(date);
        stock.setTotalStock(racket.getQuantity());
        stock.setAvailableStock(racket.getQuantity());
        stock.setReservedStock(0);
        stock.setRentalStock(0);
        return racketStockByDateRepository.save(stock);
    }
}
