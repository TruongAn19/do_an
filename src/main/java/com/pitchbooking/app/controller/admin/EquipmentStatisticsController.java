package com.pitchbooking.app.controller.admin;

import com.pitchbooking.app.domain.dto.ApiResponse;
import com.pitchbooking.app.domain.dto.ProductResponseDTO;
import com.pitchbooking.app.domain.dto.TopEquipmentDto;
import com.pitchbooking.app.service.EquipmentStatisticsService;
import com.pitchbooking.app.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/equipment-statistics")
@RequiredArgsConstructor
public class EquipmentStatisticsController {

    private final EquipmentStatisticsService statisticsService;
    private final ProductService productService;

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

        Integer totalEquipments = statisticsService.getTotalEquipments(courtId);
        int currentlyRented = statisticsService.getCurrentlyRentedEquipments(courtId);
        int rentalCount = statisticsService.getRentalCountInRange(courtId, startDate, endDate);
        double revenue = statisticsService.getRevenueInRange(courtId, startDate, endDate);
        List<TopEquipmentDto> topEquipments = statisticsService.getTopRentedEquipmentsInRange(courtId, startDate, endDate, 5);

        YearMonth currentMonth = YearMonth.now();
        YearMonth sixMonthsAgo = currentMonth.minusMonths(5);

        Map<YearMonth, Integer> rentalsByMonth = statisticsService.getRentalCountByMonthRange(courtId, sixMonthsAgo, currentMonth);
        Map<YearMonth, Double> revenueByMonth = statisticsService.getRevenueByMonthRange(courtId, sixMonthsAgo, currentMonth);

        List<ProductResponseDTO> listProduct = productService.getAllProductOptions();

        Map<String, Object> data = Map.of(
                "listProduct", listProduct,
                "totalEquipments", totalEquipments,
                "currentlyRented", currentlyRented,
                "monthlyRentals", rentalCount,
                "monthlyRevenue", revenue,
                "topEquipments", topEquipments,
                "rentalsByMonth", rentalsByMonth,
                "revenueByMonth", revenueByMonth
        );

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thành công").data(data).build());
    }
}
