package com.pitchbooking.app.service.pricing;

import com.pitchbooking.app.domain.AvailableTime;
import com.pitchbooking.app.domain.BookingType;
import com.pitchbooking.app.domain.Product;
import com.pitchbooking.app.domain.User;
import com.pitchbooking.app.domain.dto.BookingPriceBreakdown;
import com.pitchbooking.app.domain.dto.PendingBookingData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final List<PricingStrategy> strategies;

    public double calculateFinalPrice(double basePrice, BookingContext context) {
        double finalPrice = basePrice;

        for (PricingStrategy strategy : strategies) {
            finalPrice = strategy.calculatePrice(finalPrice, context);
        }

        return finalPrice;
    }

    public double calculateRecurringDiscountRate(Integer durationMonths) {
        if (durationMonths == null)
            return 0;
        if (durationMonths >= 3)
            return 0.10; // 10%
        if (durationMonths >= 2)
            return 0.08; // 8%
        if (durationMonths >= 1)
            return 0.05; // 5%
        return 0;
    }

    /**
     * Computes the per-slot list + totals + deposit + recurring discount for a
     * booking request. Shared by {@code BookingClientController#estimatePrice}
     * (price preview) and {@code BookingService#preparePendingBooking} (confirm
     * flow) so the two cannot drift.
     *
     * <p>For WEEKLY_RECURRING, the end date is derived from {@code durationMonths}
     * when present, otherwise {@code recurringEndDate} is used. Throws
     * {@link IllegalArgumentException} when the request is missing the bits
     * needed to enumerate a non-empty date list.
     */
    public BookingPriceBreakdown calculateBookingPriceBreakdown(
            User user,
            Product product,
            AvailableTime time,
            BookingType bookingType,
            LocalDate startDate,
            LocalDate recurringEndDate,
            List<Integer> daysOfWeek,
            Integer durationMonths) {

        BookingType type = (bookingType != null) ? bookingType : BookingType.ONE_TIME;
        List<LocalDate> datesToBook = new ArrayList<>();
        LocalDate finalEndDate = recurringEndDate;

        if (type == BookingType.WEEKLY_RECURRING) {
            if (durationMonths != null && durationMonths > 0) {
                finalEndDate = startDate.plusMonths(durationMonths);
            }
            if (finalEndDate == null) {
                throw new IllegalArgumentException("Thiếu thông tin thời hạn đặt sân cố định.");
            }
            if (finalEndDate.isBefore(startDate)) {
                throw new IllegalArgumentException("Ngày kết thúc chu kỳ không thể trước ngày bắt đầu.");
            }
            if (daysOfWeek == null || daysOfWeek.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn ít nhất một thứ trong tuần.");
            }
            LocalDate current = startDate;
            while (!current.isAfter(finalEndDate)) {
                if (daysOfWeek.contains(current.getDayOfWeek().getValue())) {
                    datesToBook.add(current);
                }
                current = current.plusDays(1);
            }
        } else {
            datesToBook.add(startDate);
        }

        if (datesToBook.isEmpty()) {
            throw new IllegalArgumentException("Không có ngày nào hợp lệ trong khoảng thời gian đã chọn.");
        }

        double basePrice = product.getPrice() - (product.getPrice() * product.getSale() / 100.0);
        double discountRate = calculateRecurringDiscountRate(durationMonths);
        // Deposit = price × (1 − sale/100) × 0.5 per session (50% rule).
        double perSessionDeposit = product.getPrice() * (1 - product.getSale() / 100.0) * 0.5;

        double totalBookingPrice = 0;
        List<PendingBookingData.SlotData> slots = new ArrayList<>();

        for (LocalDate date : datesToBook) {
            BookingContext context = BookingContext.builder()
                    .user(user)
                    .time(time)
                    .bookingDate(date)
                    .build();
            double finalPriceForSlot = calculateFinalPrice(basePrice, context);
            if (type == BookingType.WEEKLY_RECURRING) {
                finalPriceForSlot = finalPriceForSlot * (1 - discountRate);
            }
            totalBookingPrice += finalPriceForSlot;
            slots.add(new PendingBookingData.SlotData(date, finalPriceForSlot, product.getSale()));
        }

        double depositPrice = perSessionDeposit * datesToBook.size();
        if (type == BookingType.WEEKLY_RECURRING) {
            depositPrice = depositPrice * (1 - discountRate);
        }

        double savings = (basePrice * datesToBook.size()) - totalBookingPrice;

        return new BookingPriceBreakdown(slots, totalBookingPrice, depositPrice, discountRate, savings);
    }
}
