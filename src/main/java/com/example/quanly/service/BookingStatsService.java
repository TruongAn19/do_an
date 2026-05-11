package com.example.quanly.service;

import com.example.quanly.domain.BookingStatus;
import com.example.quanly.repository.BookingDetailRepository;
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

    public Map<String, Double> getRevenueBetweenDates(LocalDate start, LocalDate end) {
        List<BookingStatus> statuses = List.of(BookingStatus.DA_THANH_TOAN, BookingStatus.DA_DAT);
        List<Object[]> results = bookingDetailRepository.getDailyRevenueBetweenDates(start, end, statuses);
        Map<String, Double> data = new LinkedHashMap<>();
        for (Object[] result : results) {
            String date = result[0].toString();
            Double revenue = (Double) result[1];
            data.put(date, revenue);
        }
        return data;
    }
}
