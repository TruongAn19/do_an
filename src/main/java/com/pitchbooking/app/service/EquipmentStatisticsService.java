package com.pitchbooking.app.service;

import com.pitchbooking.app.domain.Equipment;
import com.pitchbooking.app.domain.dto.TopEquipmentDto;
import com.pitchbooking.app.repository.EquipmentRepository;
import com.pitchbooking.app.repository.EquipmentStockByDateRepository;
import com.pitchbooking.app.repository.RentalToolRepository;

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
public class EquipmentStatisticsService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentStockByDateRepository equipmentStockByDateRepository;
    private final RentalToolRepository rentalToolRepository;

    /**
     * 1. Tổng số thiết bị hiện có trong kho của một sân
     */
    public Integer getTotalEquipments(Long courtId) {
        Integer total = equipmentRepository.countEquipmentByProductId(courtId);
        return total == null ? 0 : total;
    }

    /**
     * 2. Số thiết bị đang cho thuê hiện tại tại một sân
     */
    public int getCurrentlyRentedEquipments(Long courtId) {
        LocalDate today = LocalDate.now();
        Integer sum = equipmentStockByDateRepository.sumRentalStockByCourtAndDate(courtId, today);
        if (sum == null) {
            sum = 0;
        }
        return sum;
    }

    /**
     * 3. Số lượt thuê thiết bị trong tháng (theo DAILY rental)
     */
    public int getRentalCountInRange(Long courtId, LocalDate startDate, LocalDate endDate) {
        return rentalToolRepository.countDailyRentalByCourtAndDateRange(courtId, startDate, endDate);
    }

    /**
     * 4. Doanh thu từ thuê thiết bị trong tháng (theo DAILY rental)
     */
    public Double getRevenueInRange(Long courtId, LocalDate startDate, LocalDate endDate) {
        return rentalToolRepository.sumDailyRevenueByCourtAndDateRange(courtId, startDate, endDate);
    }

    /**
     * 5. Top thiết bị được thuê nhiều nhất trong tháng (theo DAILY rental)
     */
    public List<TopEquipmentDto> getTopRentedEquipmentsInRange(Long courtId, LocalDate startDate, LocalDate endDate,
            int limit) {
        // Tạo một đối tượng Pageable với số lượng giới hạn (limit)
        Pageable pageable = (Pageable) PageRequest.of(0, limit);

        List<Object[]> results = rentalToolRepository.findTopDailyRentedEquipments(courtId, startDate, endDate, pageable);

        List<TopEquipmentDto> topEquipments = new ArrayList<>();
        for (Object[] result : results) {
            Equipment equipment = (Equipment) result[0];
            Long totalQuantity = (Long) result[1];
            Double totalRevenue = (Double) result[2];

            TopEquipmentDto dto = new TopEquipmentDto();
            dto.setId(equipment.getId());
            dto.setFactory(equipment.getFactory());
            dto.setName(equipment.getName());
            dto.setRentCount(totalQuantity != null ? totalQuantity.intValue() : 0);
            dto.setRevenue(totalRevenue != null ? totalRevenue : 0.0);

            topEquipments.add(dto);
        }
        return topEquipments;
    }

    public Map<YearMonth, Integer> getRentalCountByMonthRange(Long courtId, YearMonth startMonth, YearMonth endMonth) {
        Map<YearMonth, Integer> rentalCountMap = new LinkedHashMap<>();

        YearMonth current = startMonth;
        while (!current.isAfter(endMonth)) {
            LocalDate monthStart = current.atDay(1);
            LocalDate monthEnd = current.atEndOfMonth();

            int rentalCount = rentalToolRepository.countDailyRentalByCourtAndDateRange(
                    courtId, monthStart, monthEnd);

            rentalCountMap.put(current, rentalCount);

            current = current.plusMonths(1); // sang tháng tiếp theo
        }

        return rentalCountMap;
    }

    // Lấy doanh thu theo từng tháng trong khoảng thời gian
    public Map<YearMonth, Double> getRevenueByMonthRange(Long courtId, YearMonth startMonth, YearMonth endMonth) {
        Map<YearMonth, Double> revenueMap = new LinkedHashMap<>();
        YearMonth current = startMonth;
        while (!current.isAfter(endMonth)) {
            LocalDate monthStart = current.atDay(1);
            LocalDate monthEnd = current.atEndOfMonth();
            Double revenue = rentalToolRepository.sumDailyRevenueByCourtAndDateRange(
                    courtId, monthStart, monthEnd);
            revenueMap.put(current, revenue);
            current = current.plusMonths(1); // sang tháng tiếp theo
        }
        return revenueMap;
    }
}
