package com.example.quanly.service.pricing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final List<PricingStrategy> strategies;

    public double calculateFinalPrice(double basePrice, BookingContext context) {
        double finalPrice = basePrice;

        // Áp dụng tất cả các chiến lược (ví dụ: Peak Hour sau đó mới tính VIP Discount)
        // Lưu ý: Thứ tự các Bean trong List có thể quan trọng nếu tính % chồng nhau.
        // Ở đây chúng ta giả định các strategy tính toán dựa trên basePrice được truyền
        // vào.

        for (PricingStrategy strategy : strategies) {
            // Nếu muốn tính gộp, ta có thể cập nhật basePrice cho vòng lặp sau.
            // Nhưng đơn giản nhất là mỗi strategy trả về giá mới.
            finalPrice = strategy.calculatePrice(finalPrice, context);
        }

        return finalPrice;
    }
}
