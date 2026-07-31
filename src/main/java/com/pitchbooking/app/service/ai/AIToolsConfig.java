package com.pitchbooking.app.service.ai;

import com.pitchbooking.app.domain.AvailableTime;
import com.pitchbooking.app.domain.SubPitch;
import com.pitchbooking.app.domain.BookingDetail;
import com.pitchbooking.app.domain.TemporaryBooking;
import com.pitchbooking.app.repository.BookingDetailRepository;
import com.pitchbooking.app.repository.SubPitchRepository;
import com.pitchbooking.app.repository.SubPitchAvailableTimeRepository;
import com.pitchbooking.app.repository.TemporaryBookingRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AIToolsConfig {

    private final SubPitchRepository subPitchRepository;
    private final SubPitchAvailableTimeRepository subPitchAvailableTimeRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final TemporaryBookingRepository temporaryBookingRepository;
    public AIToolsConfig(SubPitchRepository subPitchRepository,
            SubPitchAvailableTimeRepository subPitchAvailableTimeRepository,
            BookingDetailRepository bookingDetailRepository,
            TemporaryBookingRepository temporaryBookingRepository) {
        this.subPitchRepository = subPitchRepository;
        this.subPitchAvailableTimeRepository = subPitchAvailableTimeRepository;
        this.bookingDetailRepository = bookingDetailRepository;
        this.temporaryBookingRepository = temporaryBookingRepository;
    }

    public record PitchInfo(String clusterName, String pitchName, String region, String addressDetail) {}
    public record AllPitchesResponse(List<PitchInfo> pitches) {}

    @Tool(description = "Liệt kê danh sách tất cả các sân bóng đá, bao gồm tên sân, khu vực (Hà Nội, HCM...) và địa chỉ chi tiết.")
    public AllPitchesResponse listAllPitches() {
        List<SubPitch> pitches = subPitchRepository.findAll();
        List<PitchInfo> infoList = pitches.stream().map(p -> {
            String cluster = p.getProduct() != null ? p.getProduct().getName() : "Chưa xác định";
            String region = p.getProduct() != null ? p.getProduct().getAddress() : "Chưa có khu vực";
            String detail = p.getProduct() != null ? p.getProduct().getAddressDetail() : "Chưa có địa chỉ chi tiết";
            return new PitchInfo(cluster, p.getName(), region, detail);
        }).collect(Collectors.toList());
        return new AllPitchesResponse(infoList);
    }

    public record PitchAvailabilityRequest(String date) {
    }

    public record PitchAvailabilityResponse(String date, List<String> availableSlots) {
    }

    @Tool(description = "Kiểm tra lịch trống của các sân bóng đá theo ngày. Tham số date phải có định dạng YYYY-MM-DD.")
    public PitchAvailabilityResponse checkPitchAvailability(PitchAvailabilityRequest request) {
        try {
            LocalDate date = LocalDate.parse(request.date());
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();

            List<SubPitch> allPitches = subPitchRepository.findAll();

            List<String> availableSlots = new ArrayList<>();

            for (SubPitch pitch : allPitches) {
                List<AvailableTime> configuredTimes =
                        subPitchAvailableTimeRepository.findAvailableTimesBySubPitch(pitch);
                List<BookingDetail> bookings = bookingDetailRepository.findBySubPitchAndDate(pitch, date);
                Set<Long> bookedTimeIds = bookings.stream()
                        .map(b -> b.getAvailableTime().getId())
                        .collect(Collectors.toSet());

                List<TemporaryBooking> temporaryBookings = temporaryBookingRepository
                        .findBySubPitchAndBookingDate(pitch, date);
                Set<Long> heldTimeIds = temporaryBookings.stream()
                        .filter(tb -> !tb.isExpired())
                        .map(tb -> tb.getAvailableTime().getId())
                        .collect(Collectors.toSet());

                for (AvailableTime time : configuredTimes) {
                    if (date.equals(today) && time.getTime().isBefore(now))
                        continue;

                    if (!bookedTimeIds.contains(time.getId()) && !heldTimeIds.contains(time.getId())) {
                        String productName = pitch.getProduct() != null ? pitch.getProduct().getName()
                                : "Sân mặc định";
                        availableSlots.add(productName + " (" + pitch.getName() + ") - Giờ: " + time.getTime());
                    }
                }
            }

            if (availableSlots.isEmpty()) {
                availableSlots.add("Không còn sân trống nào trong ngày này.");
            } else if (availableSlots.size() > 50) {
                List<String> subList = new ArrayList<>(availableSlots.subList(0, 50));
                subList.add("... (Và còn rất nhiều khung giờ khác)");
                return new PitchAvailabilityResponse(request.date(), subList);
            }

            return new PitchAvailabilityResponse(request.date(), availableSlots);
        } catch (Exception e) {
            return new PitchAvailabilityResponse(request.date(),
                    List.of("Lỗi định dạng ngày. Vui lòng cung cấp ngày theo định dạng YYYY-MM-DD."));
        }
    }

}
