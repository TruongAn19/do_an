package com.example.quanly.service.ai;

import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.SubCourt;
import com.example.quanly.domain.BookingDetail;
import com.example.quanly.domain.SubCourtAvailableTime;
import com.example.quanly.domain.TemporaryBooking;
import com.example.quanly.repository.BookingDetailRepository;
import com.example.quanly.repository.SubCourtAvailableTimeRepository;
import com.example.quanly.repository.SubCourtRepository;
import com.example.quanly.repository.TemporaryBookingRepository;
import com.example.quanly.service.BookingStatsService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AIToolsConfig {

    private final SubCourtRepository subCourtRepository;
    private final SubCourtAvailableTimeRepository subCourtAvailableTimeRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final TemporaryBookingRepository temporaryBookingRepository;
    private final BookingStatsService bookingStatsService;

    public AIToolsConfig(SubCourtRepository subCourtRepository,
            SubCourtAvailableTimeRepository subCourtAvailableTimeRepository,
            BookingDetailRepository bookingDetailRepository,
            TemporaryBookingRepository temporaryBookingRepository,
            BookingStatsService bookingStatsService) {
        this.subCourtRepository = subCourtRepository;
        this.subCourtAvailableTimeRepository = subCourtAvailableTimeRepository;
        this.bookingDetailRepository = bookingDetailRepository;
        this.temporaryBookingRepository = temporaryBookingRepository;
        this.bookingStatsService = bookingStatsService;
    }

    public record CourtInfo(String clusterName, String courtName, String region, String addressDetail) {
    }

    public record AllCourtsResponse(List<CourtInfo> courts) {
    }

    public AllCourtsResponse listAllCourts() {
        List<SubCourt> courts = subCourtRepository.findActiveCourts();
        List<CourtInfo> infoList = courts.stream().map(c -> {
            String cluster = c.getProduct() != null ? c.getProduct().getName() : "Chưa xác định";
            String region = c.getProduct() != null ? c.getProduct().getAddress() : "Chưa có khu vực";
            String detail = c.getProduct() != null ? c.getProduct().getAddressDetail() : "Chưa có địa chỉ chi tiết";
            return new CourtInfo(cluster, c.getName(), region, detail);
        }).collect(Collectors.toList());
        return new AllCourtsResponse(infoList);
    }

    public record CourtAvailabilityRequest(String date) {
    }

    public record CourtAvailabilityResponse(String date, List<String> availableSlots) {
    }

    public CourtAvailabilityResponse checkCourtAvailability(CourtAvailabilityRequest request) {
        String requestedDate = request == null ? null : request.date();
        try {
            if (requestedDate == null || requestedDate.isBlank()) {
                return availabilityError(requestedDate,
                        "Thiếu ngày cần kiểm tra. Vui lòng cung cấp ngày theo định dạng YYYY-MM-DD.");
            }
            LocalDate date = LocalDate.parse(requestedDate);
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();
            if (date.isBefore(today)) {
                return availabilityError(requestedDate, "Không thể kiểm tra lịch trống cho ngày trong quá khứ.");
            }

            List<SubCourt> allCourts = subCourtRepository.findActiveCourts();

            List<String> availableSlots = new ArrayList<>();

            for (SubCourt court : allCourts) {
                List<AvailableTime> configuredTimes = subCourtAvailableTimeRepository.findBySubCourt(court).stream()
                        .map(SubCourtAvailableTime::getAvailableTime)
                        .filter(java.util.Objects::nonNull)
                        .sorted(Comparator.comparing(AvailableTime::getTime))
                        .toList();

                List<BookingDetail> bookings = bookingDetailRepository.findBySubCourtAndDate(court, date);
                Set<Long> bookedTimeIds = bookings.stream()
                        .map(BookingDetail::getAvailableTime)
                        .filter(java.util.Objects::nonNull)
                        .map(AvailableTime::getId)
                        .collect(Collectors.toSet());

                List<TemporaryBooking> temporaryBookings = temporaryBookingRepository
                        .findBySubCourtAndBookingDate(court, date);
                Set<Long> heldTimeIds = temporaryBookings.stream()
                        .filter(tb -> !tb.isExpired())
                        .map(TemporaryBooking::getAvailableTime)
                        .filter(java.util.Objects::nonNull)
                        .map(AvailableTime::getId)
                        .collect(Collectors.toSet());

                for (AvailableTime time : configuredTimes) {
                    if (time.getTime() == null) {
                        continue;
                    }
                    if (date.equals(today) && !time.getTime().isAfter(now))
                        continue;

                    if (!bookedTimeIds.contains(time.getId()) && !heldTimeIds.contains(time.getId())) {
                        String productName = court.getProduct() != null ? court.getProduct().getName()
                                : "Sân mặc định";
                        availableSlots.add(productName + " (" + court.getName() + ") - Giờ: " + time.getTime());
                    }
                }
            }

            if (availableSlots.isEmpty()) {
                availableSlots.add("Không còn sân trống nào trong ngày này.");
            } else if (availableSlots.size() > 50) {
                List<String> subList = new ArrayList<>(availableSlots.subList(0, 50));
                subList.add("... (Và còn rất nhiều khung giờ khác)");
                return new CourtAvailabilityResponse(requestedDate, subList);
            }

            return new CourtAvailabilityResponse(requestedDate, availableSlots);
        } catch (DateTimeParseException e) {
            return availabilityError(requestedDate,
                    "Lỗi định dạng ngày. Vui lòng cung cấp ngày theo định dạng YYYY-MM-DD.");
        }
    }

    private CourtAvailabilityResponse availabilityError(String date, String message) {
        return new CourtAvailabilityResponse(date, List.of(message));
    }

    public record RevenueRequest(String startDate, String endDate) {
    }

    public record RevenueResponse(String report) {
    }

    public RevenueResponse getRevenueReport(RevenueRequest request, boolean admin) {
        if (!admin) {
            return new RevenueResponse(
                    "Báo cáo doanh thu chỉ dành cho quản trị viên. Vui lòng đăng nhập với tài khoản admin để xem.");
        }
        try {
            LocalDate start = LocalDate.parse(request.startDate());
            LocalDate end = LocalDate.parse(request.endDate());
            if (end.isBefore(start)) {
                return new RevenueResponse("Ngày kết thúc không thể trước ngày bắt đầu.");
            }

            Map<String, Double> revenueData = bookingStatsService.getRevenueBetweenDates(start, end);

            if (revenueData.isEmpty()) {
                return new RevenueResponse("Không có dữ liệu doanh thu trong khoảng "
                        + start + " đến " + end + ".");
            }

            StringBuilder report = new StringBuilder("Khoảng báo cáo: ")
                    .append(start).append(" đến ").append(end).append("\n");
            double total = 0;
            for (Map.Entry<String, Double> entry : revenueData.entrySet()) {
                report.append("- ").append(entry.getKey()).append(": ")
                        .append(String.format("%,.0f", entry.getValue())).append(" VNĐ\n");
                total += entry.getValue();
            }
            report.append("\nTổng doanh thu: ").append(String.format("%,.0f", total)).append(" VNĐ");

            return new RevenueResponse(report.toString());
        } catch (DateTimeParseException | NullPointerException e) {
            return new RevenueResponse("Lỗi định dạng ngày. Vui lòng cung cấp ngày theo định dạng YYYY-MM-DD.");
        }
    }
}
