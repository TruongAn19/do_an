package com.example.quanly.service;

import com.example.quanly.domain.Racket;
import com.example.quanly.domain.RacketStockByDate;
import com.example.quanly.domain.dto.CheckStockRequest;
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

    public RacketStockByDate getStock(CheckStockRequest request) {
        return racketStockByDateRepository.findByRacketAndDate(request.getRacketId(), request.getDate());
    }
}
