package com.example.quanly.controller.admin;

import com.example.quanly.domain.Product;
import com.example.quanly.domain.Racket;
import com.example.quanly.domain.dto.TopRacketDto;
import com.example.quanly.repository.ProductRepository;
import com.example.quanly.service.ProductService;
import com.example.quanly.service.RacketStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Controller
public class RacketStatisticsController {

    @Autowired
    private RacketStatisticsService statisticsService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/admin/racket-statistics")
    public String showStatistics(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "courtId", required = false) Long courtId,
            Model model) {

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

        Map<YearMonth, Integer> rentalsByMonth = statisticsService.getRentalCountByMonthRange(sixMonthsAgo, currentMonth);
        Map<YearMonth, Double> revenueByMonth = statisticsService.getRevenueByMonthRange(sixMonthsAgo, currentMonth);


        List<Product>  listProduct =  productRepository.findAll();
        model.addAttribute("listProduct", listProduct);
        model.addAttribute("courtId", courtId);
        model.addAttribute("totalRackets", totalRackets);
        model.addAttribute("currentlyRented", currentlyRented);
        model.addAttribute("monthlyRentals", rentalCount);
        model.addAttribute("monthlyRevenue", revenue);
        model.addAttribute("topRackets", topRackets);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("rentalsByMonth", rentalsByMonth);
        model.addAttribute("revenueByMonth", revenueByMonth);

        return "admin/dashboard/racket-statistics";
    }

}
