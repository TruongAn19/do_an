package com.example.quanly.service.ai;

import com.example.quanly.config.HoldPolicy;
import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.SubCourt;
import com.example.quanly.domain.BookingDetail;
import com.example.quanly.domain.TemporaryBooking;
import com.example.quanly.repository.BookingDetailRepository;
import com.example.quanly.repository.SubCourtRepository;
import com.example.quanly.repository.TemporaryBookingRepository;
import com.example.quanly.repository.TimeRepository;
import com.example.quanly.service.BookingStatsService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private final SubCourtRepository subCourtRepository;
    private final TimeRepository timeRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final TemporaryBookingRepository temporaryBookingRepository;
    private final BookingStatsService bookingStatsService;
    private final HoldPolicy holdPolicy;

    public AIToolsConfig(SubCourtRepository subCourtRepository, TimeRepository timeRepository,
            BookingDetailRepository bookingDetailRepository,
            TemporaryBookingRepository temporaryBookingRepository,
            BookingStatsService bookingStatsService,
            HoldPolicy holdPolicy) {
        this.subCourtRepository = subCourtRepository;
        this.timeRepository = timeRepository;
        this.bookingDetailRepository = bookingDetailRepository;
        this.temporaryBookingRepository = temporaryBookingRepository;
        this.bookingStatsService = bookingStatsService;
        this.holdPolicy = holdPolicy;
    }

    public record CourtInfo(String clusterName, String courtName, String region, String addressDetail) {}
    public record AllCourtsResponse(List<CourtInfo> courts) {}

    @Tool(description = "Liệt kê danh sách tất cả các sân cầu lông, bao gồm tên sân, khu vực (Hà Nội, HCM...) và địa chỉ chi tiết.")
    public AllCourtsResponse listAllCourts() {
        List<SubCourt> courts = subCourtRepository.findAll();
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

    @Tool(description = "Kiểm tra lịch trống của các sân cầu lông theo ngày. Tham số date phải có định dạng YYYY-MM-DD.")
    public CourtAvailabilityResponse checkCourtAvailability(CourtAvailabilityRequest request) {
        try {
            LocalDate date = LocalDate.parse(request.date());
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();

            List<SubCourt> allCourts = subCourtRepository.findAll();
            List<AvailableTime> allTimes = timeRepository.findAll();

            List<String> availableSlots = new ArrayList<>();

            for (SubCourt court : allCourts) {
                List<BookingDetail> bookings = bookingDetailRepository.findBySubCourtAndDate(court, date);
                Set<Long> bookedTimeIds = bookings.stream()
                        .map(b -> b.getAvailableTime().getId())
                        .collect(Collectors.toSet());

                List<TemporaryBooking> temporaryBookings = temporaryBookingRepository
                        .findBySubCourtAndBookingDate(court, date);
                Set<Long> heldTimeIds = temporaryBookings.stream()
                        .filter(tb -> !tb.isExpired(holdPolicy.getHoldDuration()))
                        .map(tb -> tb.getAvailableTime().getId())
                        .collect(Collectors.toSet());

                for (AvailableTime time : allTimes) {
                    if (date.equals(today) && time.getTime().isBefore(now))
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
                return new CourtAvailabilityResponse(request.date(), subList);
            }

            return new CourtAvailabilityResponse(request.date(), availableSlots);
        } catch (Exception e) {
            return new CourtAvailabilityResponse(request.date(),
                    List.of("Lỗi định dạng ngày. Vui lòng cung cấp ngày theo định dạng YYYY-MM-DD."));
        }
    }

    public record RevenueRequest(String startDate, String endDate) {
    }

    public record RevenueResponse(String report) {
    }

    @Tool(description = "Lấy báo cáo doanh thu theo khoảng thời gian. Truyền startDate và endDate theo định dạng YYYY-MM-DD. CHỈ DÀNH CHO ADMIN.")
    public RevenueResponse getRevenueReport(RevenueRequest request) {
        // Endpoint /api/v1/ai/** là permitAll nhưng JWT filter vẫn populate auth nếu token có
        // → check role tại đây để chặn user thường gõ tay "doanh thu tuần này".
        if (!hasAdminRole()) {
            return new RevenueResponse(
                    "Báo cáo doanh thu chỉ dành cho quản trị viên. Vui lòng đăng nhập với tài khoản admin để xem.");
        }
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

    /** True nếu request hiện tại đến từ user có ROLE_ADMIN (hoặc tương đương). */
    private boolean hasAdminRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
