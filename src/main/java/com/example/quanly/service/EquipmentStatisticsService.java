package com.example.quanly.service;

import com.example.quanly.domain.Equipment;
import com.example.quanly.domain.RentalToolStatus;
import com.example.quanly.domain.dto.TopEquipmentDto;
import com.example.quanly.repository.EquipmentRepository;
import com.example.quanly.repository.EquipmentStockByDateRepository;
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

        // Truy vấn để lấy các thiết bị thuê nhiều nhất
        List<Object[]> results = rentalToolRepository.findTopDailyRentedEquipments(courtId, startDate, endDate, pageable);

        List<TopEquipmentDto> topEquipments = new ArrayList<>();
        for (Object[] result : results) {
            Equipment equipment = (Equipment) result[0];
            Long rentalCount = (Long) result[1];

            TopEquipmentDto dto = new TopEquipmentDto();
            dto.setId(equipment.getId());
            dto.setFactory(equipment.getFactory());
            dto.setName(equipment.getName());
            dto.setRentalStock(rentalCount.intValue());

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

            int rentalCount = rentalToolRepository.countByRentalDateBetweenAndStatus(courtId, monthStart, monthEnd,
                    (RentalToolStatus.COMPLETED));

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
            Double revenue = rentalToolRepository.sumRevenueByRentalDateBetweenAndStatus(courtId, monthStart, monthEnd,
                    (RentalToolStatus.COMPLETED));
            if (revenue == null) {
                revenue = 0.0;
            }
            revenueMap.put(current, revenue);
            current = current.plusMonths(1); // sang tháng tiếp theo
        }
        return revenueMap;
    }
}
