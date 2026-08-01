package com.pitchbooking.app.service.ai;

import com.pitchbooking.app.domain.AvailableTime;
import com.pitchbooking.app.domain.BookingDetail;
import com.pitchbooking.app.domain.Product;
import com.pitchbooking.app.domain.SubPitch;
import com.pitchbooking.app.domain.TemporaryBooking;
import com.pitchbooking.app.repository.BookingDetailRepository;
import com.pitchbooking.app.repository.SubPitchAvailableTimeRepository;
import com.pitchbooking.app.repository.SubPitchRepository;
import com.pitchbooking.app.repository.TemporaryBookingRepository;
import com.pitchbooking.app.service.BookingStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Read-only business operations exposed to the AI model.
 *
 * <p>The model must never query JPA entities directly or mutate booking state.
 * These methods return small, explicit response objects so the model can only
 * use data that is safe and relevant to the conversation.</p>
 */
@Component
@RequiredArgsConstructor
public class AIToolsConfig {

    private static final int MAX_REPORT_DAYS = 366;

    private final SubPitchRepository subPitchRepository;
    private final SubPitchAvailableTimeRepository subPitchAvailableTimeRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final TemporaryBookingRepository temporaryBookingRepository;
    private final BookingStatsService bookingStatsService;

    public record PitchInfo(
            long productId,
            String productName,
            long subPitchId,
            String subPitchName,
            String pitchType,
            String address,
            String addressDetail,
            double basePricePerHour,
            long discountPercent) {
    }

    public record AllPitchesResponse(List<PitchInfo> pitches) {
    }

    public AllPitchesResponse listAllPitches() {
        List<PitchInfo> pitches = activePitches().stream()
                .map(this::toPitchInfo)
                .sorted(Comparator.comparing(PitchInfo::productName)
                        .thenComparing(PitchInfo::subPitchName))
                .toList();
        return new AllPitchesResponse(pitches);
    }

    public record PitchAvailabilityRequest(String date, Long productId, Long subPitchId) {
    }

    public record AvailablePitch(
            long productId,
            String productName,
            long subPitchId,
            String subPitchName,
            String pitchType,
            List<String> availableTimes) {
    }

    public record PitchAvailabilityResponse(
            String date,
            String message,
            List<AvailablePitch> pitches) {
    }

    public PitchAvailabilityResponse checkPitchAvailability(PitchAvailabilityRequest request) {
        if (request == null || request.date() == null || request.date().isBlank()) {
            return availabilityError(null, "Ngày cần kiểm tra không được để trống (định dạng YYYY-MM-DD).");
        }

        final LocalDate date;
        try {
            date = LocalDate.parse(request.date());
        } catch (DateTimeParseException ex) {
            return availabilityError(request.date(), "Ngày không đúng định dạng YYYY-MM-DD.");
        }

        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            return availabilityError(request.date(), "Không thể kiểm tra hoặc đặt sân cho ngày đã qua.");
        }

        List<SubPitch> candidates = activePitches().stream()
                .filter(pitch -> request.productId() == null
                        || pitch.getProduct().getId() == request.productId())
                .filter(pitch -> request.subPitchId() == null
                        || pitch.getId().equals(request.subPitchId()))
                .toList();

        if (candidates.isEmpty()) {
            return new PitchAvailabilityResponse(request.date(),
                    "Không tìm thấy sân đang hoạt động phù hợp với mã được cung cấp.", List.of());
        }

        LocalTime now = LocalTime.now();
        List<AvailablePitch> availability = new ArrayList<>();
        for (SubPitch pitch : candidates) {
            Set<Long> occupiedTimeIds = occupiedTimeIds(pitch, date);

            List<String> freeTimes = subPitchAvailableTimeRepository.findAvailableTimesBySubPitch(pitch).stream()
                    .filter(time -> time.getId() != null)
                    .filter(time -> !occupiedTimeIds.contains(time.getId()))
                    .filter(time -> !date.equals(today) || time.getTime().isAfter(now))
                    .sorted(Comparator.comparing(AvailableTime::getTime))
                    .map(time -> time.getTime().toString())
                    .toList();

            if (!freeTimes.isEmpty()) {
                Product product = pitch.getProduct();
                availability.add(new AvailablePitch(
                        product.getId(), product.getName(), pitch.getId(), pitch.getName(),
                        pitchTypeName(pitch), freeTimes));
            }
        }

        String message = availability.isEmpty()
                ? "Không còn khung giờ trống phù hợp trong ngày này."
                : "Các khung giờ dưới đây đang trống tại thời điểm kiểm tra; người dùng vẫn cần giữ chỗ và thanh toán để xác nhận.";
        return new PitchAvailabilityResponse(request.date(), message, availability);
    }

    public record RevenueRequest(String startDate, String endDate) {
    }

    public record RevenueResponse(
            boolean authorized,
            String startDate,
            String endDate,
            Map<String, Double> dailyRevenue,
            double totalRevenue,
            String message) {
    }

    public RevenueResponse getRevenueReport(RevenueRequest request, boolean admin) {
        if (!admin) {
            return revenueError(false, request,
                    "Báo cáo doanh thu chỉ dành cho quản trị viên.");
        }
        if (request == null || request.startDate() == null || request.endDate() == null) {
            return revenueError(true, request,
                    "Ngày bắt đầu và ngày kết thúc không được để trống.");
        }

        final LocalDate start;
        final LocalDate end;
        try {
            start = LocalDate.parse(request.startDate());
            end = LocalDate.parse(request.endDate());
        } catch (DateTimeParseException ex) {
            return revenueError(true, request, "Ngày không đúng định dạng YYYY-MM-DD.");
        }

        if (start.isAfter(end)) {
            return revenueError(true, request, "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.");
        }
        if (start.plusDays(MAX_REPORT_DAYS - 1L).isBefore(end)) {
            return revenueError(true, request, "Mỗi báo cáo chỉ được xem tối đa 366 ngày.");
        }

        Map<String, Double> dailyRevenue = bookingStatsService.getRevenueBetweenDates(start, end);
        double total = dailyRevenue.values().stream().mapToDouble(Double::doubleValue).sum();
        String message = dailyRevenue.isEmpty()
                ? "Không có doanh thu đã ghi nhận trong khoảng thời gian này."
                : "Doanh thu chỉ tính các booking đã đặt hoặc đã thanh toán.";
        return new RevenueResponse(true, start.toString(), end.toString(), dailyRevenue, total, message);
    }

    private List<SubPitch> activePitches() {
        return subPitchRepository.findAll().stream()
                .filter(pitch -> pitch.getId() != null)
                .filter(pitch -> pitch.getProduct() != null)
                .filter(pitch -> isActive(pitch.getProduct()))
                .toList();
    }

    private boolean isActive(Product product) {
        return product.getStatus() != null
                && "ACTIVE".equals(product.getStatus().trim().toUpperCase(Locale.ROOT));
    }

    private PitchInfo toPitchInfo(SubPitch pitch) {
        Product product = pitch.getProduct();
        return new PitchInfo(
                product.getId(), product.getName(), pitch.getId(), pitch.getName(),
                pitchTypeName(pitch), product.getAddress(), product.getAddressDetail(),
                product.getPrice(), product.getSale());
    }

    private String pitchTypeName(SubPitch pitch) {
        return pitch.getPitchType() == null ? "UNKNOWN" : pitch.getPitchType().name();
    }

    private Set<Long> occupiedTimeIds(SubPitch pitch, LocalDate date) {
        Set<Long> occupied = bookingDetailRepository.findBySubPitchAndDate(pitch, date).stream()
                .map(BookingDetail::getAvailableTime)
                .filter(time -> time != null && time.getId() != null)
                .map(AvailableTime::getId)
                .collect(Collectors.toSet());

        temporaryBookingRepository.findBySubPitchAndBookingDate(pitch, date).stream()
                .filter(hold -> hold.getAvailableTime() != null)
                .filter(hold -> hold.getAvailableTime().getId() != null)
                .filter(hold -> hold.getHoldExpiresAt() != null && !hold.isExpired())
                .map(TemporaryBooking::getAvailableTime)
                .map(AvailableTime::getId)
                .forEach(occupied::add);
        return occupied;
    }

    private PitchAvailabilityResponse availabilityError(String date, String message) {
        return new PitchAvailabilityResponse(date, message, List.of());
    }

    private RevenueResponse revenueError(boolean authorized, RevenueRequest request, String message) {
        String start = request == null ? null : request.startDate();
        String end = request == null ? null : request.endDate();
        return new RevenueResponse(authorized, start, end, Map.of(), 0, message);
    }
}
