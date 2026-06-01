package com.example.quanly.controller.admin;

import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.service.BookingStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/booking-statistics")
@RequiredArgsConstructor
public class BookingStatisticsController {

    private final BookingStatsService bookingStatsService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBookingStatistics(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null || endDate == null) {
            YearMonth currentMonth = YearMonth.now();
            startDate = currentMonth.atDay(1);
            endDate = currentMonth.atEndOfMonth();
        }

        Map<String, Long> countByStatus = bookingStatsService.getBookingCountByStatus(startDate, endDate);
        Map<String, Double> revenuePerCourt = bookingStatsService.getRevenueBetweenDates(startDate, endDate);

        long totalBookings = countByStatus.values().stream().mapToLong(Long::longValue).sum();
        double totalRevenue = revenuePerCourt.values().stream().mapToDouble(Double::doubleValue).sum();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("countByStatus", countByStatus);
        data.put("revenuePerCourt", revenuePerCourt);
        data.put("totalBookings", totalBookings);
        data.put("totalRevenue", totalRevenue);

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thành công").data(data).build());
    }
}
