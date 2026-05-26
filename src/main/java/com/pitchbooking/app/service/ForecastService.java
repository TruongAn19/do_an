package com.pitchbooking.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ForecastService {

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getRevenueForecast() {
        // Lấy dữ liệu doanh thu lịch sử từ DB (demo data)
        List<Map<String, Object>> historicalData = List.of(
                Map.of("ds", "2024-03-01", "y", 5000000),
                Map.of("ds", "2024-03-02", "y", 4500000));

        try {
            String mlServiceUrl = "http://localhost:5000/predict";
            // Unchecked cast: RestTemplate trả về raw List từ ML service
            return restTemplate.postForObject(mlServiceUrl, historicalData, List.class);
        } catch (Exception e) {
            // Nếu ML service chưa chạy, trả về empty hoặc mock
            return List.of();
        }
    }
}
