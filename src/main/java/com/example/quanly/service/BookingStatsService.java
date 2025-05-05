package com.example.quanly.service;

import com.example.quanly.repository.BookingDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookingStatsService {
    @Autowired
    private BookingDetailRepository bookingDetailRepository;

    public Map<String, Double> getRevenueBetweenDates(LocalDate start, LocalDate end) {
        List<Object[]> results = bookingDetailRepository.getRevenuePerProductBetweenDates(start, end);
        Map<String, Double> data = new LinkedHashMap<>();
        for (Object[] result : results) {
            String productName = (String) result[0];
            Double revenue = (Double) result[1];
            data.put(productName, revenue);
        }
        return data;
    }
}
