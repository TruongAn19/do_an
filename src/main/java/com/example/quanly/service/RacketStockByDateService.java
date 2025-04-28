package com.example.quanly.service;

import com.example.quanly.domain.Racket;
import com.example.quanly.domain.RacketStockByDate;
import com.example.quanly.repository.RacketRepository;
import com.example.quanly.repository.RacketStockByDateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@EnableScheduling
public class RacketStockByDateService {


    @Autowired
    private RacketRepository racketRepository;
    @Autowired
    private RacketStockByDateRepository racketStockByDateRepository;

    @Scheduled(cron = "0 8 15 * * ?") // Mỗi ngày vào lúc 00:00
    @Transactional
    public void generateStockByDate() {
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusDays(10);
        List<Racket> allRackets = racketRepository.findAll();
        for (Racket racket : allRackets) {
            for (LocalDate date = today; !date.isAfter(targetDate); date = date.plusDays(1)) {
                // Kiểm tra nếu đã tồn tại rồi thì bỏ qua
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

}
