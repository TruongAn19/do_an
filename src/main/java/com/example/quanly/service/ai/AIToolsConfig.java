package com.example.quanly.service.ai;

import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.SubPitch;
import com.example.quanly.domain.BookingDetail;
import com.example.quanly.domain.TemporaryBooking;
import com.example.quanly.repository.BookingDetailRepository;
import com.example.quanly.repository.SubPitchRepository;
import com.example.quanly.repository.TemporaryBookingRepository;
import com.example.quanly.repository.TimeRepository;
import com.example.quanly.service.BookingStatsService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AIToolsConfig {

    private final SubPitchRepository subPitchRepository;
    private final TimeRepository timeRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final TemporaryBookingRepository temporaryBookingRepository;
    private final BookingStatsService bookingStatsService;

    public AIToolsConfig(SubPitchRepository subPitchRepository, TimeRepository timeRepository,
            BookingDetailRepository bookingDetailRepository,
            TemporaryBookingRepository temporaryBookingRepository,
            BookingStatsService bookingStatsService) {
        this.subPitchRepository = subPitchRepository;
        this.timeRepository = timeRepository;
        this.bookingDetailRepository = bookingDetailRepository;
        this.temporaryBookingRepository = temporaryBookingRepository;
        this.bookingStatsService = bookingStatsService;
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
            List<AvailableTime> allTimes = timeRepository.findAll();

            List<String> availableSlots = new ArrayList<>();

            for (SubPitch pitch : allPitches) {
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

                for (AvailableTime time : allTimes) {
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

    public record RevenueRequest(String startDate, String endDate) {
    }

    public record RevenueResponse(String report) {
    }

    @Tool(description = "Lấy báo cáo doanh thu theo khoảng thời gian. Truyền startDate và endDate theo định dạng YYYY-MM-DD.")
    public RevenueResponse getRevenueReport(RevenueRequest request) {
        try {
            LocalDate start = LocalDate.parse(request.startDate());
            LocalDate end = LocalDate.parse(request.endDate());

            Map<String, Double> revenueData = bookingStatsService.getRevenueBetweenDates(start, end);

            if (revenueData.isEmpty()) {
                return new RevenueResponse("Không có dữ liệu doanh thu trong khoảng thời gian này.");
            }

            StringBuilder report = new StringBuilder();
            double total = 0;
            for (Map.Entry<String, Double> entry : revenueData.entrySet()) {
                report.append("- ").append(entry.getKey()).append(": ")
                        .append(String.format("%,.0f", entry.getValue())).append(" VNĐ\n");
                total += entry.getValue();
            }
            report.append("\nTổng doanh thu: ").append(String.format("%,.0f", total)).append(" VNĐ");

            return new RevenueResponse(report.toString());
        } catch (Exception e) {
            return new RevenueResponse("Lỗi định dạng ngày. Vui lòng cung cấp ngày theo định dạng YYYY-MM-DD.");
        }
    }
}
