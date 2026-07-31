package com.pitchbooking.app.service;

import com.pitchbooking.app.domain.AvailableTime;
import com.pitchbooking.app.domain.BookingDetail;
import com.pitchbooking.app.domain.SubPitch;
import com.pitchbooking.app.domain.dto.AvailableTimeDTO;
import com.pitchbooking.app.repository.BookingDetailRepository;
import com.pitchbooking.app.repository.BookingRepository;
import com.pitchbooking.app.repository.SubPitchAvailableTimeRepository;
import com.pitchbooking.app.repository.SubPitchRepository;
import com.pitchbooking.app.repository.TemporaryBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final SubPitchRepository subPitchRepository;
    private final SubPitchAvailableTimeRepository subPitchAvailableTimeRepository;
    private final TemporaryBookingRepository temporaryBookingRepository;

    public List<AvailableTimeDTO> recommendSlots(Long userId, Long productId) {
        // 1. Tìm khung giờ hay đặt nhất
        Long frequentTimeId = bookingRepository.findMostFrequentTimeSlotByUserId(userId);

        // 2. Lấy tất cả các sân thuộc sản phẩm này
        List<SubPitch> courts = subPitchRepository.findByProductId(productId);

        // 3. Kiểm tra xem hôm nay hoặc ngày mai, khung giờ này có trống ở sân nào không
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<AvailableTime> configuredTimes = courts.stream()
                .flatMap(court -> subPitchAvailableTimeRepository
                        .findAvailableTimesBySubPitch(court).stream())
                .collect(Collectors.toMap(
                        AvailableTime::getId,
                        time -> time,
                        (left, right) -> left,
                        LinkedHashMap::new))
                .values().stream()
                .toList();

        return configuredTimes.stream()
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

                    for (SubPitch court : courts) {
                        if (subPitchAvailableTimeRepository
                                .findBySubPitchAndAvailableTime(court, t)
                                .isEmpty()) {
                            continue;
                        }
                        Optional<BookingDetail> opt = bookingDetailRepository
                                .findBySubPitchAndAvailableTimeAndDate(court, t, today);
                        boolean held = temporaryBookingRepository
                                .findBySubPitchAndBookingDate(court, today).stream()
                                .anyMatch(temporary -> !temporary.isExpired()
                                        && temporary.getAvailableTime().getId().equals(t.getId()));
                        if (opt.isEmpty() && !held)
                            return true; // Còn ít nhất 1 sân trống
                    }
                    return false;
                })
                .limit(3) // Gợi ý tối đa 3 khung giờ
                .map(AvailableTimeDTO::new)
                .collect(Collectors.toList());
    }
}
