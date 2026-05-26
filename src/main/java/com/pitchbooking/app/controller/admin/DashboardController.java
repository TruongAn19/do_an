package com.pitchbooking.app.controller.admin;

import com.pitchbooking.app.domain.dto.ApiResponse;
import com.pitchbooking.app.service.ProductService;
import com.pitchbooking.app.service.EquipmentService;
import com.pitchbooking.app.service.UserService;
import com.pitchbooking.app.repository.BookingRepository;
import com.pitchbooking.app.domain.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class DashboardController {
    private final UserService userService;
    private final EquipmentService equipmentService;
    private final ProductService productService;
    private final BookingRepository bookingRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        
        var topProductsRaw = bookingRepository.findTopProducts(PageRequest.of(0, 5));
        var topProducts = topProductsRaw.stream().map(row -> Map.of(
                "name", row[0],
                "bookingCount", row[1],
                "revenue", row[2]
        )).collect(Collectors.toList());

        var recentBookingsRaw = bookingRepository.findRecentBookings(PageRequest.of(0, 5));
        var recentBookings = recentBookingsRaw.stream().map(b -> Map.of(
                "id", b.getId(),
                "totalPrice", b.getTotalPrice()
        )).collect(Collectors.toList());

        Double r1 = bookingRepository.sumTotalPriceByStatusAndDateBetween(BookingStatus.DA_THANH_TOAN, startOfMonth, now);
        Double r2 = bookingRepository.sumTotalPriceByStatusAndDateBetween(BookingStatus.DA_DAT, startOfMonth, now);
        double totalMonthlyRevenue = (r1 != null ? r1 : 0) + (r2 != null ? r2 : 0);

        Map<String, Object> data = Map.of(
                "countUser", userService.countUser(),
                "countProduct", productService.getCourtProduct(),
                "countByEquipment", equipmentService.countEquipment(),
                "countBookingToday", bookingRepository.countByBookingDate(now),
                "monthlyRevenue", totalMonthlyRevenue,
                "topProducts", topProducts,
                "recentBookings", recentBookings
        );
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thành công").data(data).build());
    }
}
