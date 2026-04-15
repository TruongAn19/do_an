package com.example.quanly.controller.admin;

import com.example.quanly.domain.Product;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.domain.dto.TopRacketDto;
import com.example.quanly.repository.ProductRepository;
import com.example.quanly.service.RacketStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/racket-statistics")
@RequiredArgsConstructor
public class RacketStatisticsController {

    private final RacketStatisticsService statisticsService;
    private final ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "courtId", required = false) Long courtId) {

        if (startDate == null || endDate == null) {
            YearMonth currentMonth = YearMonth.now();
            startDate = currentMonth.atDay(1);
            endDate = currentMonth.atEndOfMonth();
        }

        Integer totalRackets = statisticsService.getTotalRackets(courtId);
        int currentlyRented = statisticsService.getCurrentlyRentedRackets(courtId);
        int rentalCount = statisticsService.getRentalCountInRange(courtId, startDate, endDate);
        double revenue = statisticsService.getRevenueInRange(courtId, startDate, endDate);
        List<TopRacketDto> topRackets = statisticsService.getTopRentedRacketsInRange(courtId, startDate, endDate, 5);

        YearMonth currentMonth = YearMonth.now();
        YearMonth sixMonthsAgo = currentMonth.minusMonths(5);

        Map<YearMonth, Integer> rentalsByMonth = statisticsService.getRentalCountByMonthRange(courtId, sixMonthsAgo, currentMonth);
        Map<YearMonth, Double> revenueByMonth = statisticsService.getRevenueByMonthRange(courtId, sixMonthsAgo, currentMonth);

        List<Product> listProduct = productRepository.findAll();

        Map<String, Object> data = Map.of(
                "listProduct", listProduct,
                "totalRackets", totalRackets,
                "currentlyRented", currentlyRented,
                "monthlyRentals", rentalCount,
                "monthlyRevenue", revenue,
                "topRackets", topRackets,
                "rentalsByMonth", rentalsByMonth,
                "revenueByMonth", revenueByMonth
        );

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thành công").data(data).build());
    }
}
