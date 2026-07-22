package com.example.quanly.controller.admin;

import com.example.quanly.domain.Product;
import com.example.quanly.domain.RentalType;
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
import java.util.LinkedHashMap;
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
        Map<String, Object> daily = statisticsService.getBreakdownByType(courtId, startDate, endDate, RentalType.DAILY);
        Map<String, Object> onSite = statisticsService.getBreakdownByType(courtId, startDate, endDate, RentalType.ON_SITE);
        int totalOrders = (Integer) daily.get("orders") + (Integer) onSite.get("orders");
        long totalQuantity = (Long) daily.get("quantity") + (Long) onSite.get("quantity");
        double totalRevenue = (Double) daily.get("revenue") + (Double) onSite.get("revenue");
        List<TopRacketDto> topRackets = statisticsService.getTopRentedRacketsInRange(courtId, startDate, endDate, 5);

        YearMonth currentMonth = YearMonth.now();
        YearMonth sixMonthsAgo = currentMonth.minusMonths(5);

        Map<YearMonth, Integer> dailyOrdersByMonth = statisticsService.getOrderCountByMonthRange(courtId, sixMonthsAgo, currentMonth, RentalType.DAILY);
        Map<YearMonth, Integer> onSiteOrdersByMonth = statisticsService.getOrderCountByMonthRange(courtId, sixMonthsAgo, currentMonth, RentalType.ON_SITE);
        Map<YearMonth, Double> dailyRevenueByMonth = statisticsService.getRevenueByMonthRange(courtId, sixMonthsAgo, currentMonth, RentalType.DAILY);
        Map<YearMonth, Double> onSiteRevenueByMonth = statisticsService.getRevenueByMonthRange(courtId, sixMonthsAgo, currentMonth, RentalType.ON_SITE);

        List<Product> listProduct = productRepository.findAll();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("listProduct", listProduct);
        data.put("totalRackets", totalRackets);
        data.put("currentlyRented", currentlyRented);
        data.put("monthlyOrders", totalOrders);
        data.put("monthlyQuantity", totalQuantity);
        data.put("monthlyRevenue", totalRevenue);
        data.put("daily", daily);
        data.put("onSite", onSite);
        data.put("topRackets", topRackets);
        data.put("dailyOrdersByMonth", dailyOrdersByMonth);
        data.put("onSiteOrdersByMonth", onSiteOrdersByMonth);
        data.put("dailyRevenueByMonth", dailyRevenueByMonth);
        data.put("onSiteRevenueByMonth", onSiteRevenueByMonth);

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thành công").data(data).build());
    }
}
