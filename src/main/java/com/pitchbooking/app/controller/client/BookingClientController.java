package com.pitchbooking.app.controller.client;

import com.pitchbooking.app.domain.Booking;
import com.pitchbooking.app.domain.BookingDetail;
import com.pitchbooking.app.domain.BookingStatus;
import com.pitchbooking.app.domain.BookingType;
import com.pitchbooking.app.domain.RentalTool;
import com.pitchbooking.app.domain.RentalToolStatus;
import com.pitchbooking.app.domain.SubPitch;
import com.pitchbooking.app.domain.Product;
import com.pitchbooking.app.domain.AvailableTime;
import com.pitchbooking.app.domain.TemporaryBooking;
import com.pitchbooking.app.domain.User;
import com.pitchbooking.app.domain.Equipment;
import com.pitchbooking.app.domain.dto.ApiResponse;
import com.pitchbooking.app.mapper.EquipmentResponseMapper;
import com.pitchbooking.app.exception.ResourceNotFoundException;
import com.pitchbooking.app.domain.dto.AvailableTimeDTO;
import com.pitchbooking.app.domain.dto.CancelBookingRequest;
import com.pitchbooking.app.domain.dto.CancelBookingResponse;
import com.pitchbooking.app.domain.dto.HoldBookingRequest;
import com.pitchbooking.app.domain.dto.NotificationDTO;
import com.pitchbooking.app.domain.dto.PreparedBookingResult;
import com.pitchbooking.app.domain.NotificationType;
import com.pitchbooking.app.domain.RefundStatus;
import com.pitchbooking.app.domain.PaymentType;
import com.pitchbooking.app.domain.dto.PaymentRequest;
import com.pitchbooking.app.domain.dto.PlaceBookingRequest;
import com.pitchbooking.app.domain.dto.EstimatePriceRequest;
import com.pitchbooking.app.domain.dto.EstimatePriceResponse;
import com.pitchbooking.app.domain.dto.ProductResponseDTO;
import jakarta.validation.Valid;
import com.pitchbooking.app.domain.dto.VnpayResponse;
import com.pitchbooking.app.repository.*;
import com.pitchbooking.app.service.BookingService;
import com.pitchbooking.app.service.NotificationService;
import com.pitchbooking.app.service.NtfyService;
import com.pitchbooking.app.service.PaymentService;
import com.pitchbooking.app.service.ProductService;
import com.pitchbooking.app.service.EquipmentService;
import com.pitchbooking.app.service.RecommendationService;
import com.pitchbooking.app.service.pricing.PricingService;
import com.pitchbooking.app.util.SecurityUtils;
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
        SubPitchAvailableTimeRepository subPitchAvailableTimeRepository;
        TimeRepository timeRepository;
        BookingDetailRepository bookingDetailRepository;
        BookingService bookingService;
        PaymentService paymentService;
        TemporaryBookingRepository temporaryBookingRepository;
        RecommendationService recommendationService;
        SecurityUtils securityUtils;
        NtfyService ntfyService;
        NotificationService notificationService;
        EquipmentResponseMapper equipmentResponseMapper;
        PricingService pricingService;

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
                List<SubPitch> courts = productService.getAllCourtsByProduct(productId);
                // A product may have sub-pitches with different schedules. Do not expose
                // global slots here; return the configured union and let /available-times
                // return the exact schedule after the user selects a sub-pitch.
                List<AvailableTime> configuredTimes = courts.stream()
                                .flatMap(court -> subPitchAvailableTimeRepository
                                                .findAvailableTimesBySubPitch(court).stream())
                                .collect(Collectors.toMap(AvailableTime::getId, time -> time, (left, right) -> left))
                                .values().stream()
                                .sorted(java.util.Comparator.comparing(AvailableTime::getTime))
                                .toList();

                double price = product.getPrice();
                double totalPrice = price - (price * product.getSale() / 100.0);
                List<Equipment> equipments = equipmentService.getAvailableEquipmentsByCourt(productId);

                Map<String, Object> data = Map.of(
                                "product", product,
                                "courts", courts,
                                "availableTimes", configuredTimes,
                                "equipments", equipmentResponseMapper.toDTOs(equipments),
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
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Không tìm thấy sân phụ ID: " + courtId));

                List<BookingDetail> bookings = bookingDetailRepository.findBySubPitchAndDate(court, date);
                Set<Long> bookedTimeIds = bookings.stream()
                                .map(b -> b.getAvailableTime().getId())
                                .collect(Collectors.toSet());

                // Lấy danh sách các khung giờ đang bị giữ tạm thời và chưa hết hạn
                List<TemporaryBooking> temporaryBookings = temporaryBookingRepository
                                .findBySubPitchAndBookingDate(court, date);
                Set<Long> heldTimeIds = temporaryBookings.stream()
                                .filter(tb -> !tb.isExpired())
                                .map(tb -> tb.getAvailableTime().getId())
                                .collect(Collectors.toSet());

                LocalDate today = LocalDate.now();
                LocalTime now = LocalTime.now();
                log.info("Date: {}, CourtId: {}", date, courtId);

                List<AvailableTimeDTO> result = subPitchAvailableTimeRepository
                                .findAvailableTimesBySubPitch(court).stream()
                                .filter(time -> {
                                        if (date.equals(today) && time.getTime().isBefore(now))
                                                return false;
                                        // Không hiển thị nếu đã đặt chính thức HOẶC đang bị giữ chỗ
                                        return !bookedTimeIds.contains(time.getId())
                                                        && !heldTimeIds.contains(time.getId());
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

                temporaryBookingRepository.deleteExpiredHolds();
                temporaryBookingRepository.flush();

                // Lock an existing, stable row. Locking TemporaryBooking alone cannot
                // serialize two first-time inserts because no hold row exists yet.
                SubPitch court = subPitchRepository.findByIdWithLock(holdRequest.getSubPitchId())
                                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sân phụ"));
                AvailableTime time = timeRepository.findById(holdRequest.getAvailableTimeId())
                                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khung giờ"));

                if (subPitchAvailableTimeRepository
                                .findBySubPitchAndAvailableTime(court, time)
                                .isEmpty()) {
                        throw new IllegalArgumentException(
                                        "Khung giờ không được cấu hình cho sân phụ đã chọn.");
                }

                Optional<TemporaryBooking> existingOpt = temporaryBookingRepository
                                .findBySubPitchAndAvailableTimeAndBookingDateWithLock(court, time,
                                                holdRequest.getBookingDate());

                LocalDateTime now = LocalDateTime.now();

                if (existingOpt.isPresent()) {
                        TemporaryBooking existing = existingOpt.get();
                        if (existing.isExpired()) {
                                temporaryBookingRepository.delete(existing);
                                temporaryBookingRepository.flush();
                        } else {
                                if (!existing.getUserId().equals(userId)) {
                                        long remaining = Math.max(
                                                        Duration.between(now, existing.getHoldExpiresAt()).getSeconds(),
                                                        0);
                                        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse
                                                        .<Map<String, Object>>builder()
                                                        .status(409)
                                                        .message("Khung giờ này đang được giữ. Vui lòng thử lại sau.")
                                                        .data(Map.of("remainingTime", remaining))
                                                        .build());
                                } else {
                                        existing.setHoldExpiresAt(now.plusMinutes(3));
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
                newHold.setHoldExpiresAt(now.plusMinutes(3));
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
        public ResponseEntity<ApiResponse<EstimatePriceResponse>> estimatePrice(
                        @Valid @RequestBody EstimatePriceRequest request) {
                        Product product = productService.getRawProductById(request.getProductId());
                        AvailableTime time = timeRepository.findById(request.getAvailableTimeId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khung giờ"));

                        com.pitchbooking.app.domain.dto.BookingPriceBreakdown breakdown = pricingService
                                .calculateBookingPriceBreakdown(getCurrentUser(), product, time,
                                                        request.getBookingType(), request.getBookingDate(),
                                                        request.getRecurringEndDate(), request.getDaysOfWeek(),
                                                        request.getDurationMonths());

                        double basePrice = product.getPrice() - (product.getPrice() * product.getSale() / 100.0);

                        EstimatePriceResponse data = EstimatePriceResponse.builder()
                                        .basePrice(basePrice).sessions(breakdown.getSlots().size())
                                        .totalPrice(breakdown.getTotalPrice()).depositPrice(breakdown.getDepositPrice())
                                        .savings(breakdown.getSavings()).discountRate(breakdown.getDiscountRate() * 100)
                                        .remainingPrice(breakdown.getTotalPrice() - breakdown.getDepositPrice()).build();
                        return ResponseEntity.ok(ApiResponse.<EstimatePriceResponse>builder()
                                        .status(200).message("Ước tính giá thành công").data(data).build());
        }

        @PostMapping("/place")
        public ResponseEntity<ApiResponse<Map<String, Object>>> placeBooking(
                        @Valid @RequestBody PlaceBookingRequest req,
                        HttpServletRequest request) {

                User currentUser = getCurrentUser();

                PreparedBookingResult prepared = bookingService.preparePendingBooking(currentUser,
                                req.getReceiverName(), req.getReceiverAddress(), req.getReceiverPhone(),
                                req.getProductId(), req.getAvailableTimeId(), req.getCourtId(), req.getBookingDate(),
                                req.getBookingType(), req.getRecurringEndDate(),
                                req.getDaysOfWeek(), req.getDurationMonths(), req.getEquipments());

                PaymentRequest paymentRequest = new PaymentRequest();
                paymentRequest.setId(prepared.pendingId());
                paymentRequest.setAmount(prepared.paymentAmount());
                paymentRequest.setType(PaymentType.PENDING_BOOKING);
                paymentRequest.setRedirectUrl("");

                VnpayResponse vnpayResponse = paymentService.createVnPayPayment(paymentRequest, request);

                Map<String, Object> data = Map.of(
                                "paymentUrl", vnpayResponse.getPaymentUrl(),
                                "depositPrice", prepared.depositPrice(),
                                "equipmentRentalPrice", prepared.equipmentRentalPrice(),
                                "paymentAmount", prepared.paymentAmount());

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<Map<String, Object>>builder()
                                                .status(HttpStatus.CREATED.value())
                                                .message("Vui lòng hoàn tất thanh toán để xác nhận đặt sân").data(data)
                                                .build());
        }

        /**
         * Fetch a single booking owned by the caller — backs the
         * /booking-detail/:id FE page. 404 if it doesn't exist or 403 if
         * the caller is not the owner (admins should use admin endpoints).
         */
        @GetMapping("/detail/{bookingId}")
        public ResponseEntity<ApiResponse<com.pitchbooking.app.domain.dto.BookingResponseDTO>> getBookingDetail(
                        @PathVariable long bookingId) {
                User currentUser = getCurrentUser();
                com.pitchbooking.app.domain.dto.BookingResponseDTO dto = bookingService.fetchBookingById(bookingId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Không tìm thấy đơn đặt sân ID: " + bookingId));
                if (dto.getUser() == null || dto.getUser().getId() != currentUser.getId()) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(ApiResponse.<com.pitchbooking.app.domain.dto.BookingResponseDTO>builder()
                                                        .status(403).message("Không có quyền truy cập").build());
                }
                return ResponseEntity.ok(ApiResponse.<com.pitchbooking.app.domain.dto.BookingResponseDTO>builder()
                                .status(200).message("Thành công").data(dto).build());
        }

        @GetMapping("/{bookingCode}/equipments")
        public ResponseEntity<ApiResponse<Map<String, Object>>> getBookingEquipments(
                        @PathVariable String bookingCode) {

                User currentUser = getCurrentUser();
                List<Equipment> equipmentList = bookingService.getAvailableEquipmentsForBooking(
                                bookingCode, currentUser.getId());
                Map<String, Object> data = Map.of(
                                "equipments", equipmentResponseMapper.toDTOs(equipmentList),
                                "bookingCode", bookingCode);
                return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                                .status(200).message("Thành công").data(data).build());
        }

        /**
         * Spec-compliant cancel endpoint (CANCEL_BOOKING_FEATURE §4 — client).
         * Returns full refund summary + contact info. Triggers WS fan-out:
         *  - 1 notification to the cancelling user (BOOKING_CANCELLED)
         *  - 1 notification per admin/staff (REFUND_REQUEST) — skipped if refund=0 (E11).
         */
        @PostMapping("/{bookingId}/cancel")
        public ResponseEntity<ApiResponse<CancelBookingResponse>> cancelBookingV2(
                        @PathVariable long bookingId,
                        @RequestBody(required = false) CancelBookingRequest req) {
                User currentUser = getCurrentUser();
                String reason = req != null ? req.getReason() : null;

                CancelBookingResponse resp = bookingService.cancelByUser(bookingId, currentUser.getId(), reason);

                // Notification — user
                String userMsg;
                if (resp.getRefundStatus() == RefundStatus.PENDING_REFUND) {
                        userMsg = String.format(
                                        "Bạn đã huỷ thành công. Số tiền cọc hoàn dự kiến: %,.0fđ. "
                                                        + "Khoản hoàn đang chờ quản trị viên xử lý.",
                                        resp.getRefundAmount());
                        if (resp.getContactHotline() != null && !resp.getContactHotline().isBlank()) {
                                userMsg += " Hotline: " + resp.getContactHotline() + ".";
                        }
                        if (resp.getContactEmail() != null && !resp.getContactEmail().isBlank()) {
                                userMsg += " Email: " + resp.getContactEmail() + ".";
                        }
                } else {
                        userMsg = "Bạn đã huỷ thành công. Không có khoản cọc nào cần hoàn.";
                }
                NotificationDTO userNotif = notificationService.create(
                                currentUser.getId(),
                                NotificationType.BOOKING_CANCELLED,
                                "BOOKING", bookingId,
                                "Huỷ đặt sân thành công",
                                userMsg);
                notificationService.pushToUser(currentUser.getId(), userNotif);

                // Notification — admin/staff fan-out (D0.6). Skip if no refund needed (E11).
                if (resp.getRefundStatus() == RefundStatus.PENDING_REFUND) {
                        String staffTitle = "Yêu cầu hoàn cọc mới";
                        String staffMsg = String.format(
                                        "User %s vừa huỷ booking #%d — cần hoàn %,.0fđ.",
                                        currentUser.getEmail(), bookingId, resp.getRefundAmount());
                        List<Long> staffIds = notificationService.staffAndAdminUserIds();
                        NotificationDTO broadcastDto = null;
                        for (Long staffId : staffIds) {
                                NotificationDTO n = notificationService.create(
                                                staffId, NotificationType.REFUND_REQUEST,
                                                "BOOKING", bookingId, staffTitle, staffMsg);
                                if (broadcastDto == null) {
                                        broadcastDto = n; // payload for /topic broadcast (all staff)
                                }
                        }
                        if (broadcastDto != null) {
                                notificationService.pushToStaff(broadcastDto);
                        }
                }

                return ResponseEntity.ok(ApiResponse.<CancelBookingResponse>builder()
                                .status(200).message("Hủy lịch đặt sân thành công").data(resp).build());
        }

        /** Legacy path — keeps old FE clients working; routes through the same logic. */
        @DeleteMapping("/{bookingId}")
        public ResponseEntity<ApiResponse<Void>> cancelBooking(@PathVariable long bookingId) {
                User currentUser = getCurrentUser();
                bookingService.cancelByUser(bookingId, currentUser.getId(), null);
                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .status(200).message("Hủy lịch đặt sân thành công").build());
        }

        private User getCurrentUser() {
                return securityUtils.getCurrentUser();
        }
}
