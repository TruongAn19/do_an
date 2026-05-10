package com.example.quanly.controller.client;

import com.example.quanly.domain.*;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.exception.ResourceNotFoundException;
import com.example.quanly.domain.dto.AvailableTimeDTO;
import com.example.quanly.domain.dto.HoldBookingRequest;
import com.example.quanly.domain.dto.PreparedBookingResult;
import com.example.quanly.domain.PaymentType;
import com.example.quanly.domain.dto.PaymentRequest;
import com.example.quanly.domain.dto.PlaceBookingRequest;
import com.example.quanly.domain.dto.ProductResponseDTO;
import jakarta.validation.Valid;
import com.example.quanly.domain.dto.VnpayResponse;
import com.example.quanly.repository.*;
import com.example.quanly.service.BookingService;
import com.example.quanly.service.NtfyService;
import com.example.quanly.service.PaymentService;
import com.example.quanly.service.ProductService;
import com.example.quanly.service.EquipmentService;
import com.example.quanly.service.RecommendationService;
import com.example.quanly.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/client/bookings")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BookingClientController {

    ProductService productService;
    EquipmentService equipmentService;
    SubPitchRepository subPitchRepository;
    TimeRepository timeRepository;
    BookingDetailRepository bookingDetailRepository;
    BookingService bookingService;
    PaymentService paymentService;
    TemporaryBookingRepository temporaryBookingRepository;
    RecommendationService recommendationService;
    SecurityUtils securityUtils;
    NtfyService ntfyService;

    @GetMapping("/recommend/{productId}")
    public ResponseEntity<ApiResponse<List<AvailableTimeDTO>>> getRecommendations(
            @PathVariable Long productId) {

        User user = getCurrentUser();
        List<AvailableTimeDTO> recs = recommendationService.recommendSlots(user.getId(), productId);
        return ResponseEntity.ok(ApiResponse.<List<AvailableTimeDTO>>builder()
                .status(200).message("Gợi ý cho bạn").data(recs).build());
    }

    @GetMapping("/{productId}/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBookingInfo(
            @PathVariable long productId) {

        ProductResponseDTO product = productService.getProductByID(productId);
        List<AvailableTime> allTimes = productService.getAllTime();
        List<SubPitch> courts = productService.getAllCourtsByProduct(productId);

        double price = product.getPrice();
        double totalPrice = price - (price * product.getSale() / 100.0);

        Map<String, Object> data = Map.of(
                "product", product,
                "courts", courts,
                "availableTimes", allTimes,
                "totalPrice", totalPrice);

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thành công").data(data).build());
    }

    @GetMapping("/available-times")
    public ResponseEntity<ApiResponse<List<AvailableTimeDTO>>> getAvailableTimes(
            @RequestParam("date") String dateStr,
            @RequestParam("courtId") Long courtId) {

        LocalDate date = LocalDate.parse(dateStr);
        SubPitch court = subPitchRepository.findById(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân phụ ID: " + courtId));

        List<BookingDetail> bookings = bookingDetailRepository.findBySubPitchAndDate(court, date);
        Set<Long> bookedTimeIds = bookings.stream()
                .map(b -> b.getAvailableTime().getId())
                .collect(Collectors.toSet());

        // Lấy danh sách các khung giờ đang bị giữ tạm thời và chưa hết hạn
        List<TemporaryBooking> temporaryBookings = temporaryBookingRepository.findBySubPitchAndBookingDate(court, date);
        Set<Long> heldTimeIds = temporaryBookings.stream()
                .filter(tb -> !tb.isExpired())
                .map(tb -> tb.getAvailableTime().getId())
                .collect(Collectors.toSet());

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        log.info("Date: {}, CourtId: {}", date, courtId);

        List<AvailableTimeDTO> result = timeRepository.findAll().stream()
                .filter(time -> {
                    if (date.equals(today) && time.getTime().isBefore(now))
                        return false;
                    // Không hiển thị nếu đã đặt chính thức HOẶC đang bị giữ chỗ
                    return !bookedTimeIds.contains(time.getId()) && !heldTimeIds.contains(time.getId());
                })
                .map(AvailableTimeDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<AvailableTimeDTO>>builder()
                .status(200).message("Thành công").data(result).build());
    }

    @PostMapping("/hold")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> holdCourt(
            @Valid @RequestBody HoldBookingRequest holdRequest) {

        User currentUser = getCurrentUser();
        Long userId = currentUser.getId();

        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(3);
        temporaryBookingRepository.deleteExpiredHolds(expiryTime);
        temporaryBookingRepository.flush();

        SubPitch court = subPitchRepository.findById(holdRequest.getSubPitchId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sân phụ"));
        AvailableTime time = timeRepository.findById(holdRequest.getAvailableTimeId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khung giờ"));

        Optional<TemporaryBooking> existingOpt = temporaryBookingRepository
                .findBySubPitchAndAvailableTimeAndBookingDateWithLock(court, time, holdRequest.getBookingDate());

        LocalDateTime now = LocalDateTime.now();

        if (existingOpt.isPresent()) {
            TemporaryBooking existing = existingOpt.get();
            if (existing.isExpired()) {
                temporaryBookingRepository.delete(existing);
                temporaryBookingRepository.flush();
            } else {
                if (!existing.getUserId().equals(userId)) {
                    long elapsed = Duration.between(existing.getHoldStartTime(), now).getSeconds();
                    long remaining = Math.max(180 - elapsed, 0);
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse
                            .<Map<String, Object>>builder()
                            .status(409)
                            .message("Khung giờ này đang được giữ. Vui lòng thử lại sau.")
                            .data(Map.of("remainingTime", remaining))
                            .build());
                } else {
                    existing.setHoldStartTime(now);
                    temporaryBookingRepository.save(existing);
                    return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                            .status(200).message("Tiếp tục giữ sân tạm thời")
                            .data(Map.of("remainingTime", 180)).build());
                }
            }
        }

        TemporaryBooking newHold = new TemporaryBooking();
        newHold.setSubPitch(court);
        newHold.setAvailableTime(time);
        newHold.setBookingDate(holdRequest.getBookingDate());
        newHold.setUserId(userId);
        newHold.setHoldStartTime(now);
        temporaryBookingRepository.save(newHold);

        broadcastSlotHeld(holdRequest.getSubPitchId(), holdRequest.getAvailableTimeId(),
                holdRequest.getBookingDate(), userId, currentUser.getEmail());

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Giữ sân tạm thời thành công")
                .data(Map.of("remainingTime", 180)).build());
    }

    private void broadcastSlotHeld(Long subPitchId, Long availableTimeId, LocalDate bookingDate,
            Long holderUserId, String holderEmail) {
        try {
            String topic = "slot-" + subPitchId + "-" + bookingDate;
            String payload = String.format(
                    "{\"action\":\"HELD\",\"subPitchId\":%d,\"availableTimeId\":%d,\"bookingDate\":\"%s\",\"holderUserId\":%d,\"holderEmail\":\"%s\"}",
                    subPitchId, availableTimeId, bookingDate, holderUserId,
                    holderEmail == null ? "" : holderEmail.replace("\"", "\\\""));
            ntfyService.sendNotification(topic, payload, null);
        } catch (Exception e) {
            log.warn("Không thể phát thông báo realtime cho slot held: {}", e.getMessage());
        }
    }

    @PostMapping("/estimate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> estimatePrice(
            @RequestBody Map<String, Object> body) {

        try {
            long productId = Long.parseLong(body.get("productId").toString());
            long timeId = Long.parseLong(body.get("availableTimeId").toString());
            String dateStr = body.get("bookingDate").toString();
            String bookingType = body.getOrDefault("bookingType", "ONE_TIME").toString();
            LocalDate bookingDate = LocalDate.parse(dateStr);

            com.example.quanly.domain.Product product = productService.getRawProductById(productId);
            com.example.quanly.domain.AvailableTime time = timeRepository.findById(timeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khung giờ"));

            double basePrice = product.getPrice() - (product.getPrice() * product.getSale() / 100.0);

            boolean isWeekend = bookingDate.getDayOfWeek().getValue() >= 6;
            boolean isPeakTime = time.getTime().isAfter(LocalTime.of(16, 59))
                    && time.getTime().isBefore(LocalTime.of(22, 1));
            boolean hasSurcharge = isWeekend || isPeakTime;
            double surchargeRate = hasSurcharge ? 0.3 : 0.0;
            double finalPrice = hasSurcharge ? basePrice * 1.3 : basePrice;

            // Tính số buổi nếu đặt định kỳ
            int sessions = 1;
            if ("WEEKLY_RECURRING".equals(bookingType) && body.containsKey("recurringEndDate")) {
                LocalDate endDate = LocalDate.parse(body.get("recurringEndDate").toString());
                LocalDate cur = bookingDate;
                while (!cur.isAfter(endDate)) { sessions++; cur = cur.plusWeeks(1); }
            }

            double totalPrice = finalPrice * sessions;
            double depositPrice = product.getDepositPrice() * sessions;

            String surchargeReason = hasSurcharge
                    ? (isWeekend && isPeakTime ? "Cuối tuần & Giờ cao điểm (17h-22h)" :
                       isWeekend ? "Ngày cuối tuần" : "Giờ cao điểm (17h-22h)")
                    : null;

            Map<String, Object> data = new java.util.LinkedHashMap<>();
            data.put("basePrice", basePrice);
            data.put("hasSurcharge", hasSurcharge);
            data.put("surchargeRate", surchargeRate);
            data.put("surchargeReason", surchargeReason);
            data.put("pricePerSession", finalPrice);
            data.put("sessions", sessions);
            data.put("totalPrice", totalPrice);
            data.put("depositPrice", depositPrice);
            data.put("remainingPrice", totalPrice - depositPrice);

            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .status(200).message("Ước tính giá thành công").data(data).build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<Map<String, Object>>builder()
                    .status(400).message("Không thể tính giá: " + e.getMessage()).data(null).build());
        }
    }

    @PostMapping("/place")
    public ResponseEntity<ApiResponse<Map<String, Object>>> placeBooking(
            @Valid @RequestBody PlaceBookingRequest req,
            HttpServletRequest request) {

        User currentUser = getCurrentUser();

        PreparedBookingResult prepared = bookingService.preparePendingBooking(currentUser,
                req.getReceiverName(), req.getReceiverAddress(), req.getReceiverPhone(),
                req.getProductId(), req.getAvailableTimeId(), req.getCourtId(), req.getBookingDate(),
                req.getBookingType(), req.getRecurringEndDate());

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setId(prepared.pendingId());
        paymentRequest.setAmount(prepared.depositPrice());
        paymentRequest.setType(PaymentType.PENDING_BOOKING);
        paymentRequest.setRedirectUrl("");

        VnpayResponse vnpayResponse = paymentService.createVnPayPayment(paymentRequest, request);

        Map<String, Object> data = Map.of("paymentUrl", vnpayResponse.getPaymentUrl());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<Map<String, Object>>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Vui lòng hoàn tất thanh toán để xác nhận đặt sân").data(data).build());
    }

    @GetMapping("/{bookingCode}/{courtId}/equipments")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBookingEquipments(
            @PathVariable String bookingCode,
            @PathVariable long courtId) {

        List<Equipment> equipmentList = equipmentService.getAvailableEquipmentsByCourt(courtId);
        Map<String, Object> data = Map.of(
                "equipments", equipmentList,
                "bookingCode", bookingCode);
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thành công").data(data).build());
    }

    private User getCurrentUser() {
        return securityUtils.getCurrentUser();
    }
}
