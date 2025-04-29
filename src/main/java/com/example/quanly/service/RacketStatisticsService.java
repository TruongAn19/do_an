package com.example.quanly.service;


import com.example.quanly.domain.Racket;
import com.example.quanly.domain.RentalToolStatus;
import com.example.quanly.domain.dto.TopRacketDto;
import com.example.quanly.repository.RacketRepository;
import com.example.quanly.repository.RacketStockByDateRepository;
import com.example.quanly.repository.RentalToolRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RacketStatisticsService {

    private final RacketRepository racketRepository;
    private final RacketStockByDateRepository racketStockByDateRepository;
    private final RentalToolRepository rentalToolRepository;
    /**
     * 1. Tổng số vợt hiện có trong kho của một sân
     */
    public Integer getTotalRackets(Long courtId) {
        Integer total = racketRepository.countRacketByProductId(courtId);
        return total == null ? 0 : total;
    }

    /**
     * 2. Số vợt đang cho thuê hiện tại tại một sân
     */
    public int getCurrentlyRentedRackets(Long courtId) {
        LocalDate today = LocalDate.now();
        Integer sum = racketStockByDateRepository.sumRentalStockByCourtAndDate(courtId, today);
        if (sum == null) {
            sum = 0;
        }
        return sum;
    }

    /**
     * 3. Số lượt thuê vợt trong tháng (theo DAILY rental)
     */
    public int getRentalCountInRange(Long courtId, LocalDate startDate, LocalDate endDate) {
        return rentalToolRepository.countDailyRentalByCourtAndDateRange(courtId, startDate, endDate);
    }

    /**
     * 4. Doanh thu từ thuê vợt trong tháng (theo DAILY rental)
     */
    public Double getRevenueInRange(Long courtId, LocalDate startDate, LocalDate endDate) {
        return   rentalToolRepository.sumDailyRevenueByCourtAndDateRange(courtId, startDate, endDate);
    }

    /**
     * 5. Top vợt được thuê nhiều nhất trong tháng (theo DAILY rental)
     */
    public List<TopRacketDto> getTopRentedRacketsInRange(Long courtId, LocalDate startDate, LocalDate endDate,  int limit) {
        // Tạo một đối tượng Pageable với số lượng giới hạn (limit)
        Pageable pageable = (Pageable) PageRequest.of(0, limit);

        // Truy vấn để lấy các vợt thuê nhiều nhất
        List<Object[]> results = rentalToolRepository.findTopDailyRentedRackets(courtId, startDate, endDate, pageable);

        List<TopRacketDto> topRackets = new ArrayList<>();
        for (Object[] result : results) {
            Racket racket = (Racket) result[0];
            Long rentalCount = (Long) result[1];

            TopRacketDto dto = new TopRacketDto();
            dto.setId(racket.getId());
            dto.setName(racket.getName());
            dto.setRentalStock(rentalCount.intValue());

            topRackets.add(dto);
        }
        return topRackets;
    }

    public Map<YearMonth, Integer> getRentalCountByMonthRange(YearMonth startMonth, YearMonth endMonth) {
        Map<YearMonth, Integer> rentalCountMap = new LinkedHashMap<>();

        YearMonth current = startMonth;
        while (!current.isAfter(endMonth)) {
            LocalDate monthStart = current.atDay(1);
            LocalDate monthEnd = current.atEndOfMonth();

            int rentalCount = rentalToolRepository.countByRentalDateBetweenAndStatus(monthStart, monthEnd, (RentalToolStatus.COMPLETED));

            rentalCountMap.put(current, rentalCount);

            current = current.plusMonths(1); // sang tháng tiếp theo
        }

        return rentalCountMap;
    }

    // Lấy doanh thu theo từng tháng trong khoảng thời gian
    public Map<YearMonth, Double> getRevenueByMonthRange(YearMonth startMonth, YearMonth endMonth) {
        Map<YearMonth, Double> revenueMap = new LinkedHashMap<>();
        YearMonth current = startMonth;
        while (!current.isAfter(endMonth)) {
            LocalDate monthStart = current.atDay(1);
            LocalDate monthEnd = current.atEndOfMonth();
            Double revenue = rentalToolRepository.sumRevenueByRentalDateBetweenAndStatus(monthStart, monthEnd, (RentalToolStatus.COMPLETED));
            if (revenue == null) {
                revenue = 0.0;
            }
            revenueMap.put(current, revenue);
            current = current.plusMonths(1); // sang tháng tiếp theo
        }
        return revenueMap;
    }
}
