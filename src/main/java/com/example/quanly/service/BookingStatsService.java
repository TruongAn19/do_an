package com.example.quanly.service;

import com.example.quanly.domain.BookingStatus;
import com.example.quanly.repository.BookingDetailRepository;
import com.example.quanly.repository.BookingRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BookingStatsService {

    BookingDetailRepository bookingDetailRepository;
    BookingRepository bookingRepository;

    public Map<String, Double> getRevenueBetweenDates(LocalDate start, LocalDate end) {
        List<Object[]> results = bookingDetailRepository.getRevenuePerProductBetweenDates(start, end, BookingStatus.DA_THANH_TOAN);
        Map<String, Double> data = new LinkedHashMap<>();
        for (Object[] result : results) {
            String productName = (String) result[0];
            Double revenue = (Double) result[1];
            data.put(productName, revenue);
        }
        return data;
    }

    /** Số đơn đặt sân theo trạng thái (label) trong khoảng — dùng cho thống kê dashboard. */
    public Map<String, Long> getBookingCountByStatus(LocalDate start, LocalDate end) {
        List<Object[]> results = bookingRepository.countByStatusBetween(start, end);
        Map<String, Long> data = new LinkedHashMap<>();
        for (Object[] result : results) {
            BookingStatus status = (BookingStatus) result[0];
            Long count = (Long) result[1];
            data.put(status.getLabel(), count);
        }
        return data;
    }
}
