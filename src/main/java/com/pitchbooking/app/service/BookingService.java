package com.pitchbooking.app.service;

import com.pitchbooking.app.domain.Booking;
import com.pitchbooking.app.domain.BookingDetail;
import com.pitchbooking.app.domain.BookingStatus;
import com.pitchbooking.app.domain.BookingType;
import com.pitchbooking.app.domain.Equipment;
import com.pitchbooking.app.domain.RefundStatus;
import com.pitchbooking.app.domain.RentalTool;
import com.pitchbooking.app.domain.RentalToolStatus;
import com.pitchbooking.app.domain.RentalType;
import com.pitchbooking.app.domain.SubPitch;
import com.pitchbooking.app.domain.Product;
import com.pitchbooking.app.domain.AvailableTime;
import com.pitchbooking.app.domain.TemporaryBooking;
import com.pitchbooking.app.domain.User;
import com.pitchbooking.app.domain.dto.BookingResponseDTO;
import com.pitchbooking.app.domain.dto.CancelBookingResponse;
import com.pitchbooking.app.domain.dto.PendingBookingData;
import com.pitchbooking.app.domain.dto.PreparedBookingResult;
import com.pitchbooking.app.config.ContactProperties;
import com.pitchbooking.app.mapper.BookingMapper;
import com.pitchbooking.app.repository.*;
import com.pitchbooking.app.service.pricing.PricingService;
import com.pitchbooking.app.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BookingService {

    BookingRepository bookingRepository;
    BookingDetailRepository bookingDetailRepository;
    RentalToolRepository rentalToolRepository;
    EquipmentRepository equipmentRepository;
    UserRepository userRepository;
    ProductRepository productRepository;
    TimeRepository timeRepository;
    SubPitchRepository subPitchRepository;
    TemporaryBookingRepository temporaryBookingRepository;
    BookingMapper bookingMapper;
    PricingService pricingService;
    PendingBookingCache pendingBookingCache;
    ContactProperties contactProperties;

    public Page<BookingResponseDTO> fetchAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(bookingMapper::toDTO);
    }

    public Page<BookingResponseDTO> fetchBookingCode(String code, Pageable pageable) {
        return bookingRepository.findByBookingCodeContainingIgnoreCase(code, pageable).map(bookingMapper::toDTO);
    }

    public Page<BookingResponseDTO> fetchBookingsByDate(LocalDate date, Pageable pageable) {
        return bookingRepository.findByBookingDetailsDate(date, pageable).map(bookingMapper::toDTO);
    }

    public Optional<BookingResponseDTO> fetchBookingById(long id) {
        return this.bookingRepository.findById(id).map(bookingMapper::toDTO);
    }

    @Transactional
    public void deleteBookingById(long id) {
        Optional<Booking> bookingOptional = this.bookingRepository.findById(id);
        if (bookingOptional.isPresent()) {
            List<BookingDetail> bookingDetails = bookingOptional.get().getBookingDetails();
            this.bookingDetailRepository.deleteAllInBatch(bookingDetails);
        }
        this.bookingRepository.deleteById(id);
    }

    public void updateBooking(long id, String status) {
        Optional<Booking> bOptional = this.bookingRepository.findById(id);
        if (bOptional.isEmpty())
            return;
        Booking currentBooking = bOptional.get();
        currentBooking.setStatus(BookingStatus.fromLabel(status));
        this.bookingRepository.save(currentBooking);

        if (currentBooking.getStatus() == BookingStatus.DA_THANH_TOAN) {
            List<RentalTool> rentalTools = rentalToolRepository.findRentalToolsByBookingId(currentBooking.getId() + "");
            rentalTools.forEach(rt -> rt.setStatus(RentalToolStatus.COMPLETED));
            rentalToolRepository.saveAll(rentalTools);
        }
    }

    public List<RentalTool> getRentalToolsByBookingId(long id) {
        return rentalToolRepository.findRentalToolsByBookingId(String.valueOf(id));
    }

    public List<BookingResponseDTO> fetchBookingByUser(User user) {
        return this.bookingRepository.findByUser(user).stream()
                .map(bookingMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Validates and pre-computes booking data, caches it in memory, and extends the
     * TemporaryBooking hold to cover the VNPay payment window. Nothing is written to DB.
     */
    @Transactional
    public PreparedBookingResult preparePendingBooking(User user,
            String receiverName, String receiverAddress, String receiverPhone,
            long productId, long timeId, long subPitchId, LocalDate bookingDate,
            String bookingType, LocalDate recurringEndDate,
            List<Integer> daysOfWeek, Integer durationMonths) {

        // 1. Kiểm tra người dùng
        user = userRepository.findUserById(user.getId());
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng.");
        }

        BookingType type = (bookingType != null) ? BookingType.valueOf(bookingType) : BookingType.ONE_TIME;

        // 2. Lấy thông tin sân, khung giờ
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm ID: " + productId));
        SubPitch subPitch = subPitchRepository.findById(subPitchId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân phụ ID: " + subPitchId));
        AvailableTime time = timeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khung giờ ID: " + timeId));

        // 3. Tính toán danh sách ngày + giá (shared với /estimate)
        com.pitchbooking.app.domain.dto.BookingPriceBreakdown breakdown = pricingService
                .calculateBookingPriceBreakdown(user, product, time, type, bookingDate,
                        recurringEndDate, daysOfWeek, durationMonths);

        LocalDate finalEndDate = recurringEndDate;
        if (type == BookingType.WEEKLY_RECURRING && durationMonths != null && durationMonths > 0) {
            finalEndDate = bookingDate.plusMonths(durationMonths);
        }

        List<LocalDate> datesToBook = breakdown.getSlots().stream()
                .map(PendingBookingData.SlotData::getDate)
                .collect(Collectors.toList());

        // 4. Kiểm tra ngày đặt có hợp lệ (Tối đa 100 ngày)
        LocalDate today = LocalDate.now();
        LocalDate maxFutureDate = today.plusDays(100); // Mở rộng một chút cho gói 3 tháng
        for (LocalDate date : datesToBook) {
            if (date.isBefore(today)) {
                throw new IllegalArgumentException("Không thể đặt sân cho ngày trong quá khứ (" + date + ").");
            }
            if (date.isAfter(maxFutureDate)) {
                throw new IllegalArgumentException("Chỉ có thể đặt sân trong phạm vi 100 ngày tới.");
            }
        }

        // 5. Kiểm tra va chạm (Collision Check) cho TẤT CẢ các ngày
        List<String> conflictedDates = new ArrayList<>();
        for (LocalDate date : datesToBook) {
            Optional<BookingDetail> existingBooking = bookingDetailRepository
                    .findBySubPitchAndAvailableTimeAndDate(subPitch, time, date);
            if (existingBooking.isPresent()) {
                conflictedDates.add(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        }

        if (!conflictedDates.isEmpty()) {
            throw new IllegalArgumentException(
                    "Sân này đã bị trùng lịch vào các ngày: " + String.join(", ", conflictedDates));
        }

        // 6. Kiểm tra giữ chỗ (Hold Court) cho ngày bắt đầu (anchor date)
        Optional<TemporaryBooking> tempHold = temporaryBookingRepository
                .findBySubPitchAndAvailableTimeAndBookingDateWithLock(subPitch, time, bookingDate);

        if (tempHold.isEmpty()) {
            throw new IllegalArgumentException("Bạn cần giữ chỗ cho ngày bắt đầu trước khi xác nhận đặt.");
        }

        TemporaryBooking hold = tempHold.get();
        if (hold.isExpired()) {
            temporaryBookingRepository.delete(hold);
            throw new IllegalArgumentException("Phiên giữ chỗ đã hết hạn.");
        }

        // 7. Gia hạn giữ chỗ — kéo dài hold đến hết khung thanh toán VNPay (15 phút)
        hold.setHoldExpiresAt(LocalDateTime.now().plusMinutes(15));
        temporaryBookingRepository.save(hold);

        // 8. Lưu vào cache (chỉ ID + snapshot field cần thiết, không phải entity refs)
        PendingBookingData data = new PendingBookingData(
                hold.getId(),
                user.getId(), user.getEmail(),
                receiverName, receiverAddress, receiverPhone,
                product.getId(), time.getId(), subPitch.getId(),
                bookingDate, type, finalEndDate,
                daysOfWeek, durationMonths,
                breakdown.getTotalPrice(), breakdown.getDepositPrice(), breakdown.getSlots());

        long pendingId = pendingBookingCache.store(data);
        return new PreparedBookingResult(pendingId, breakdown.getDepositPrice());
    }

    /**
     * Called on successful VNPay payment. Performs a final collision check then
     * writes the confirmed booking to DB with DA_DAT (đã đặt cọc) status —
     * deposit đã thanh toán, phần còn lại trả tại sân khi staff confirm.
     */
    @Transactional
    public BookingResponseDTO confirmPendingBooking(PendingBookingData data) {
        // Re-fetch entities by id — PendingBookingData stores only IDs to be safely
        // round-tripped through Redis (no JPA proxy / lazy-collection issues).
        User user = userRepository.findUserById(data.getUserId());
        if (user == null) {
            throw new IllegalStateException("Không tìm thấy người dùng (id=" + data.getUserId() + ")");
        }
        Product product = productRepository.findById(data.getProductId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy sân (id=" + data.getProductId() + ")"));
        SubPitch subPitch = subPitchRepository.findById(data.getSubPitchId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy sân phụ (id=" + data.getSubPitchId() + ")"));
        AvailableTime availableTime = timeRepository.findById(data.getAvailableTimeId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy khung giờ (id=" + data.getAvailableTimeId() + ")"));

        // Final collision check — guard against a race where the hold expired
        for (PendingBookingData.SlotData slot : data.getSlots()) {
            Optional<BookingDetail> conflict = bookingDetailRepository
                    .findBySubPitchAndAvailableTimeAndDate(subPitch, availableTime, slot.getDate());
            if (conflict.isPresent()) {
                log.error("Xung đột lịch sau khi thanh toán thành công: SubPitch={}, time={}, date={}",
                        subPitch.getId(), availableTime.getId(), slot.getDate());
                throw new IllegalStateException("Sân đã bị đặt bởi người khác trong lúc thanh toán (ngày "
                        + slot.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ").");
            }
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setReceiverName(data.getReceiverName());
        booking.setReceiverAddress(data.getReceiverAddress());
        booking.setReceiverPhone(data.getReceiverPhone());
        booking.setAvailableTime(availableTime);
        booking.setBookingDate(data.getFirstBookingDate());
        booking.setBookingType(data.getBookingType());
        booking.setRecurringEndDate(data.getRecurringEndDate());
        booking.setDaysOfWeek(data.getDaysOfWeek() != null ? data.getDaysOfWeek().stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")) : null);
        booking.setDurationMonths(data.getDurationMonths());
        booking.setDepositPrice(data.getDepositPrice());
        booking.setTotalPrice(data.getTotalBookingPrice());
        booking.setStatus(BookingStatus.DA_DAT);

        Booking savedBooking = bookingRepository.save(booking);

        List<BookingDetail> details = new ArrayList<>();
        for (PendingBookingData.SlotData slot : data.getSlots()) {
            BookingDetail detail = new BookingDetail();
            detail.setBooking(savedBooking);
            detail.setProduct(product);
            detail.setPrice(slot.getPrice());
            detail.setSubPitch(subPitch);
            detail.setDate(slot.getDate());
            detail.setSale(slot.getSale());
            detail.setAvailableTime(availableTime);
            details.add(detail);
        }
        bookingDetailRepository.saveAll(details);

        Long tmpId = data.getTemporaryBookingId();
        if (tmpId != null) {
            temporaryBookingRepository.deleteById(tmpId);
        }

        return bookingMapper.toDTO(savedBooking);
    }

    /**
     * Called on failed VNPay payment. Frees the slot hold and removes the pending booking.
     */
    public void cancelPendingBooking(PendingBookingData data) {
        Long tmpId = data.getTemporaryBookingId();
        if (tmpId != null) {
            temporaryBookingRepository.deleteById(tmpId);
        }
    }

    @Transactional
    public Page<BookingResponseDTO> fetchBookingByUserWithPaging(Long userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable).map(bookingMapper::toDTO);
    }

    /**
     * @deprecated Dùng {@link #cancelByUser(long, long, String)} thay thế.
     *
     * <p>Method này chỉ là thin delegate giữ lại cho legacy {@code DELETE /{id}}
     * endpoint, không hỗ trợ {@code reason} và quan trọng hơn là <b>nuốt</b>
     * {@link com.pitchbooking.app.domain.dto.CancelBookingResponse} mà
     * {@code cancelByUser} trả ra — caller mất luôn refund amount, refund
     * status, used/total sessions, contact info để hiển thị cho user.
     *
     * <p>Caller mới nên gọi {@code cancelByUser(bookingId, user.getId(), reason)}
     * trực tiếp và dùng response để build notification + UI feedback.
     */
    @Deprecated
    @Transactional
    public void cancelBooking(long bookingId, User user) {
        cancelByUser(bookingId, user.getId(), null);
    }

    /**
     * Cancel a booking on behalf of its owner. Implements CANCEL_BOOKING_FEATURE
     * decisions D0.1–D0.4: status-gate, 2h gate for ONE_TIME, proportional refund
     * for WEEKLY, cascade ON_SITE rental → CANCELLED + restock.
     *
     * <p>Slot release is implicit: the slot-availability queries in
     * BookingDetailRepository exclude DA_HUY bookings, so we don't delete the
     * BookingDetail rows (we need them to compute usedSessions and as audit).
     *
     * <p>Notifications are produced by callers in Phase 4 — keep this method
     * dependency-free of NotificationService to avoid a tx cycle.
     */
    @Transactional
    public CancelBookingResponse cancelByUser(long bookingId, long userId, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt sân ID: " + bookingId));

        // E6 — ownership
        if (booking.getUser() == null || booking.getUser().getId() != userId) {
            throw new IllegalArgumentException("Bạn không có quyền hủy đơn đặt sân này.");
        }

        // E1 — already cancelled → 409 (caller may map this exception)
        if (booking.getStatus() == BookingStatus.DA_HUY) {
            throw new IllegalStateException("Đơn đặt sân này đã được hủy trước đó.");
        }

        // D0.1 — only cancellable while CHO_THANH_TOAN or DA_DAT.
        // E2 — DA_THANH_TOAN is terminal; reject.
        if (booking.getStatus() != BookingStatus.CHO_THANH_TOAN
                && booking.getStatus() != BookingStatus.DA_DAT) {
            throw new IllegalStateException("Đơn đã hoàn thành hoặc không ở trạng thái cho phép huỷ.");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // Compute refund per booking type (D0.2, D0.3, E3, E4)
        double refundAmount;
        int totalSessions;
        int usedSessions;

        if (booking.getBookingType() == BookingType.WEEKLY_RECURRING) {
            List<BookingDetail> details = booking.getBookingDetails() != null
                    ? booking.getBookingDetails() : List.of();
            totalSessions = details.size();
            usedSessions = (int) details.stream()
                    .filter(d -> d.getDate() != null && d.getDate().isBefore(today))
                    .count();
            int remaining = totalSessions - usedSessions;

            if (totalSessions == 0 || remaining <= 0) {
                // E4 — all sessions already passed; allow cancel but no refund.
                refundAmount = 0;
            } else {
                refundAmount = booking.getDepositPrice() * ((double) remaining / totalSessions);
            }
        } else {
            // ONE_TIME — totalSessions=1, usedSessions always 0 at cancel time.
            totalSessions = 1;
            usedSessions = 0;

            // E3 — 2h gate.
            LocalTime slotTime = booking.getAvailableTime() != null && booking.getAvailableTime().getTime() != null
                    ? booking.getAvailableTime().getTime()
                    : LocalTime.MIDNIGHT;
            LocalDateTime slotStart = booking.getBookingDate().atTime(slotTime);
            if (now.isAfter(slotStart.minusHours(2))) {
                long minutesUntilSlot = java.time.Duration.between(now, slotStart).toMinutes();
                String suffix = minutesUntilSlot > 0
                        ? " (còn " + minutesUntilSlot + " phút trước giờ bắt đầu)."
                        : ".";
                throw new IllegalArgumentException(
                        "Không thể huỷ trong vòng 2 tiếng trước giờ bắt đầu" + suffix);
            }
            refundAmount = booking.getDepositPrice();
        }

        // Cascade rental đính kèm booking (CANCEL_BOOKING_FEATURE §D0.4 + §E7):
        //   - ON_SITE (bundled): set CANCELLED + trả Equipment.bookingStockQuantity.
        //     Lý do: ON_SITE trừ booking-level stock lúc confirm, huỷ → restore.
        //   - DAILY: theo §E7, DAILY là standalone — KHÔNG gắn bookingId
        //     (RentalToolService.handleSubmitRental không bao giờ set bookingId
        //     cho DAILY; chỉ ON_SITE path qua handleOnSiteRental mới set). Vậy
        //     DAILY xuất hiện trong list này = data invariant đã vỡ.
        //
        //     PAID là case gây thiệt hại: tồn kho đã reserve trong
        //     EquipmentStockByDate cho từng ngày của rental period. Cascade thủ
        //     công ở đây sẽ phải replicate logic ngược của completeRental
        //     (availableStock += qty, reservedStock -= qty cho mỗi ngày) —
        //     nhưng đó là business rule khác hẳn (DAILY chưa bao giờ thuộc
        //     phạm vi cancel-booking). Throw thay vì âm thầm bỏ qua để corrupt
        //     EquipmentStockByDate không xảy ra; sự cố phải nổi lên để fix
        //     root cause (code nào đã gán bookingId cho DAILY?).
        //
        //     PENDING/COMPLETED/CANCELLED của DAILY: stock không ở trạng thái
        //     reserved-vì-booking-này, bỏ qua an toàn.
        List<RentalTool> bundled = rentalToolRepository.findRentalToolsByBookingId(String.valueOf(booking.getId()));
        for (RentalTool rt : bundled) {
            if (rt.getType() == RentalType.DAILY) {
                if (rt.getStatus() == RentalToolStatus.PAID) {
                    throw new IllegalStateException(
                            "DAILY rental PAID không được phép gắn booking (vi phạm invariant unbundled §E7). "
                                    + "RentalTool id=" + rt.getId() + ", bookingId=" + rt.getBookingId()
                                    + ". Cần điều tra code path nào đã gán bookingId trước khi cho phép huỷ booking này.");
                }
                continue;
            }
            if (rt.getType() != RentalType.ON_SITE) continue;
            if (rt.getStatus() == RentalToolStatus.CANCELLED) continue;
            rt.setStatus(RentalToolStatus.CANCELLED);
            rt.setUpdateAt(now);
            // Restock racket: bookingStockQuantity += rt.quantity
            if (rt.getEquipmentId() != null && rt.getQuantity() != null) {
                Optional<Equipment> eqOpt = equipmentRepository.findById(rt.getEquipmentId());
                eqOpt.ifPresent(eq -> {
                    eq.setBookingStockQuantity(eq.getBookingStockQuantity() + rt.getQuantity());
                    equipmentRepository.save(eq);
                });
            }
            rentalToolRepository.save(rt);
        }

        // Update booking
        booking.setStatus(BookingStatus.DA_HUY);
        booking.setCancelledAt(now);
        booking.setCancelReason(reason);
        booking.setRefundAmount(refundAmount);
        booking.setUsedSessionsAtCancel(usedSessions);
        booking.setTotalSessionsAtCancel(totalSessions);
        booking.setRefundStatus(refundAmount > 0 ? RefundStatus.PENDING_REFUND : RefundStatus.NOT_APPLICABLE);
        bookingRepository.save(booking);

        log.info("User {} cancelled booking {} — refund={}, used={}/{}",
                userId, bookingId, refundAmount, usedSessions, totalSessions);

        return CancelBookingResponse.builder()
                .bookingId(booking.getId())
                .status(booking.getStatus())
                .refundStatus(booking.getRefundStatus())
                .refundAmount(refundAmount)
                .usedSessions(usedSessions)
                .totalSessions(totalSessions)
                .cancelledAt(now)
                .contactHotline(contactProperties.getHotline())
                .contactEmail(contactProperties.getEmail())
                .build();
    }

    /**
     * Admin marks a pending refund as paid. Idempotent (E5): a second call when
     * already REFUNDED throws — caller should map to 409.
     */
    @Transactional
    public CancelBookingResponse confirmRefund(long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt sân ID: " + bookingId));

        if (booking.getStatus() != BookingStatus.DA_HUY) {
            throw new IllegalStateException("Chỉ huỷ rồi mới hoàn cọc được.");
        }
        if (booking.getRefundStatus() != RefundStatus.PENDING_REFUND) {
            throw new IllegalStateException("Trạng thái hoàn cọc hiện tại không hợp lệ: "
                    + booking.getRefundStatus());
        }

        booking.setRefundStatus(RefundStatus.REFUNDED);
        bookingRepository.save(booking);

        log.info("Admin confirmed refund for booking {}", bookingId);

        return CancelBookingResponse.builder()
                .bookingId(booking.getId())
                .status(booking.getStatus())
                .refundStatus(booking.getRefundStatus())
                .refundAmount(booking.getRefundAmount() != null ? booking.getRefundAmount() : 0)
                .usedSessions(booking.getUsedSessionsAtCancel())
                .totalSessions(booking.getTotalSessionsAtCancel())
                .cancelledAt(booking.getCancelledAt())
                .contactHotline(contactProperties.getHotline())
                .contactEmail(contactProperties.getEmail())
                .build();
    }
}
