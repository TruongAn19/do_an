package com.example.quanly.service;

import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.BookingDetail;
import com.example.quanly.domain.SubCourt;
import com.example.quanly.domain.dto.AvailableTimeDTO;
import com.example.quanly.repository.BookingDetailRepository;
import com.example.quanly.repository.BookingRepository;
import com.example.quanly.repository.SubCourtRepository;
import com.example.quanly.repository.TimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final BookingRepository bookingRepository;
    private final TimeRepository timeRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final SubCourtRepository subCourtRepository;

    public List<AvailableTimeDTO> recommendSlots(Long userId, Long productId) {
        // 1. Tìm khung giờ hay đặt nhất
        Long frequentTimeId = bookingRepository.findMostFrequentTimeSlotByUserId(userId);

        // 2. Lấy tất cả các sân thuộc sản phẩm này
        List<SubCourt> courts = subCourtRepository.findByProductId(productId);

        // 3. Kiểm tra xem hôm nay hoặc ngày mai, khung giờ này có trống ở sân nào không
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<AvailableTime> allTimes = timeRepository.findAll();

        return allTimes.stream()
                .filter(t -> {
                    // Ưu tiên khung giờ hay đặt nhất
                    if (frequentTimeId != null && t.getId().equals(frequentTimeId))
                        return true;
                    // Hoặc các khung giờ buổi tối (vàng)
                    return t.getTime().isAfter(LocalTime.of(17, 0));
                })
                .filter(t -> {
                    // Kiểm tra còn trống hôm nay
                    if (t.getTime().isBefore(now))
                        return false;

                    for (SubCourt court : courts) {
                        Optional<BookingDetail> opt = bookingDetailRepository
                                .findBySubCourtAndAvailableTimeAndDate(court, t, today);
                        if (opt.isEmpty())
                            return true; // Còn ít nhất 1 sân trống
                    }
                    return false;
                })
                .limit(3) // Gợi ý tối đa 3 khung giờ
                .map(AvailableTimeDTO::new)
                .collect(Collectors.toList());
    }
}
