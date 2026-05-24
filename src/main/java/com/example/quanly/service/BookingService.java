package com.example.quanly.service;

import com.example.quanly.domain.*;
import com.example.quanly.domain.dto.BookingResponseDTO;
import com.example.quanly.domain.dto.PendingBookingData;
import com.example.quanly.domain.dto.PreparedBookingResult;
import com.example.quanly.mapper.BookingMapper;
import com.example.quanly.repository.*;
import com.example.quanly.service.pricing.BookingContext;
import com.example.quanly.service.pricing.PricingService;
import com.example.quanly.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
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

    /**
     * Tổng thời gian một TemporaryBooking hold sống được trong quá trình thanh toán VNPay.
     * VNPay timeout sandbox ~15 phút, cộng buffer cho callback → 18 phút là đủ rộng nhưng
     * không quá lâu để chặn các user khác.
     *
     * <p>{@link PendingBookingCache} dùng hằng số này (cộng buffer nhỏ) cho TTL Redis để
     * Redis snapshot và DB hold expire gần như đồng thời — tránh case Redis hết trước
     * (callback không tìm được snapshot → user mất tiền không có booking).
     */
    public static final Duration PAYMENT_HOLD_WINDOW = Duration.ofMinutes(18);

    /**
     * Phải khớp với hằng số "3 phút" trong {@link com.example.quanly.domain.TemporaryBooking#isExpired()}.
     * Dùng để tính holdStartTime tương lai sao cho hold expire đúng vào {@code now + PAYMENT_HOLD_WINDOW}.
     */
    private static final Duration HOLD_EXPIRY_GRACE = Duration.ofMinutes(3);

    BookingRepository bookingRepository;
    BookingDetailRepository bookingDetailRepository;
    RentalToolRepository rentalToolRepository;
    RacketRepository racketRepository;
    UserRepository userRepository;
    ProductRepository productRepository;
    TimeRepository timeRepository;
    SubCourtRepository subCourtRepository;
    TemporaryBookingRepository temporaryBookingRepository;
    BookingMapper bookingMapper;
    PricingService pricingService;
    PendingBookingCache pendingBookingCache;
    NotificationService notificationService;
    SlotEventPublisher slotEventPublisher;

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

        // Khi booking được thanh toán đầy đủ tại sân: cascade trạng thái cho mọi rental ON_SITE
        // gắn kèm → COMPLETED, đồng thời trả lại stock vợt (user đã trả vợt khi thanh toán).
        if (currentBooking.getStatus() == BookingStatus.DA_THANH_TOAN) {
            List<RentalTool> rentalTools = rentalToolRepository.findRentalToolsByBookingId(currentBooking.getId() + "");
            for (RentalTool rt : rentalTools) {
                if (rt.getStatus() == RentalToolStatus.COMPLETED || rt.getStatus() == RentalToolStatus.CANCELLED) {
                    continue;
                }
                rt.setStatus(RentalToolStatus.COMPLETED);
                rt.setUpdateAt(LocalDateTime.now());
                // Trả lại stock cho vợt ON_SITE bundled (đã trừ lúc confirm booking)
                if (rt.getType() == RentalType.ON_SITE && rt.getRacketId() != null && rt.getQuantity() != null) {
                    racketRepository.findById(rt.getRacketId()).ifPresent(r -> {
                        r.setBookingStockQuantity(r.getBookingStockQuantity() + rt.getQuantity());
                        racketRepository.save(r);
                    });
                }
            }
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
            long productId, long timeId, long subCourtId, LocalDate bookingDate,
            String bookingType, LocalDate recurringEndDate,
            List<com.example.quanly.domain.dto.RentalItem> rackets,
            List<Integer> weekdays) {

        // 1. Kiểm tra người dùng
        user = userRepository.findUserById(user.getId());
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng.");
        }

        // 2. Tính toán danh sách ngày cần đặt
        BookingType type = (bookingType != null) ? BookingType.valueOf(bookingType) : BookingType.ONE_TIME;

        // D0.1: Không cho thuê vợt kèm WEEKLY_RECURRING booking
        boolean hasRackets = rackets != null && !rackets.isEmpty();
        if (hasRackets && type == BookingType.WEEKLY_RECURRING) {
            throw new IllegalArgumentException(
                    "Hiện chưa hỗ trợ thuê vợt cho đặt sân định kỳ hàng tuần. Vui lòng đặt vợt riêng sau.");
        }
        List<LocalDate> datesToBook = new ArrayList<>();
        if (type == BookingType.WEEKLY_RECURRING && recurringEndDate != null) {
            if (recurringEndDate.isBefore(bookingDate)) {
                throw new IllegalArgumentException("Ngày kết thúc chu kỳ không thể trước ngày bắt đầu.");
            }

            // Nếu user chọn weekdays cụ thể (vd Thứ 3=2, Thứ 5=4) → book mọi ngày trong [start, end]
            // có day-of-week ∈ weekdays. ISO day-of-week: 1=Mon ... 7=Sun.
            // Fallback (weekdays null/rỗng): dùng day-of-week của bookingDate (giữ behavior cũ).
            java.util.Set<Integer> weekdaySet = (weekdays != null && !weekdays.isEmpty())
                    ? new java.util.HashSet<>(weekdays)
                    : java.util.Collections.singleton(bookingDate.getDayOfWeek().getValue());

            LocalDate cursor = bookingDate;
            while (!cursor.isAfter(recurringEndDate)) {
                if (weekdaySet.contains(cursor.getDayOfWeek().getValue())) {
                    datesToBook.add(cursor);
                }
                cursor = cursor.plusDays(1);
            }

            if (datesToBook.isEmpty()) {
                throw new IllegalArgumentException(
                        "Không có ngày nào trong chu kỳ trùng với các thứ đã chọn. Vui lòng kiểm tra lại.");
            }
        } else {
            datesToBook.add(bookingDate);
        }

        // 3. Kiểm tra ngày đặt có hợp lệ (không ở quá khứ)
        LocalDate today = LocalDate.now();
        for (LocalDate date : datesToBook) {
            if (date.isBefore(today)) {
                throw new IllegalArgumentException("Không thể đặt sân cho ngày trong quá khứ (" + date + ").");
            }
        }

        // 4. Lấy thông tin sân, khung giờ
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm ID: " + productId));
        SubCourt subCourt = subCourtRepository.findById(subCourtId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân phụ ID: " + subCourtId));
        AvailableTime time = timeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khung giờ ID: " + timeId));

        // 5. Nếu đặt cho ngày hôm nay, kiểm tra khung giờ
        if (bookingDate.equals(today)) {
            LocalTime currentTime = LocalTime.now();
            if (time.getTime().isBefore(currentTime)) {
                throw new IllegalArgumentException("Khung giờ đã qua, vui lòng chọn giờ khác.");
            }
        }

        // 6. Kiểm tra va chạm (Collision Check) cho TẤT CẢ các ngày
        for (LocalDate date : datesToBook) {
            Optional<BookingDetail> existingBooking = bookingDetailRepository
                    .findActiveBySubCourtAndAvailableTimeAndDate(subCourt, time, date);
            if (existingBooking.isPresent()) {
                throw new IllegalArgumentException("Sân này đã bị trùng lịch vào ngày "
                        + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".");
            }
        }

        // 7. Kiểm tra giữ chỗ (Hold Court) cho ngày đầu tiên
        Optional<TemporaryBooking> tempHold = temporaryBookingRepository
                .findBySubCourtAndAvailableTimeAndBookingDateWithLock(subCourt, time, bookingDate);

        if (tempHold.isEmpty()) {
            throw new IllegalArgumentException("Bạn cần giữ chỗ cho ngày đầu tiên trước khi xác nhận đặt.");
        }

        TemporaryBooking hold = tempHold.get();
        if (hold.isExpired()) {
            temporaryBookingRepository.delete(hold);
            throw new IllegalArgumentException("Phiên giữ chỗ đã hết hạn.");
        }

        if (!hold.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("Sân đang được giữ bởi người khác.");
        }

        // 7b. WEEKLY_RECURRING: acquire hold cho TẤT CẢ ngày còn lại trong chu kỳ để chống
        // race condition (trước đây chỉ ngày đầu được hold → người khác có thể đặt chèn
        // sân các tuần sau giữa lúc user A đang thanh toán).
        LocalDateTime extendedHoldStart = computeExtendedHoldStart(LocalDateTime.now());
        List<TemporaryBooking> additionalHolds = new ArrayList<>();
        if (type == BookingType.WEEKLY_RECURRING && datesToBook.size() > 1) {
            for (int i = 1; i < datesToBook.size(); i++) {
                LocalDate date = datesToBook.get(i);
                Optional<TemporaryBooking> existingOpt = temporaryBookingRepository
                        .findBySubCourtAndAvailableTimeAndBookingDateWithLock(subCourt, time, date);

                if (existingOpt.isPresent()) {
                    TemporaryBooking existing = existingOpt.get();
                    if (existing.isExpired()) {
                        temporaryBookingRepository.delete(existing);
                        temporaryBookingRepository.flush();
                    } else if (!existing.getUserId().equals(user.getId())) {
                        throw new IllegalArgumentException("Sân vào ngày "
                                + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                + " đang được giữ bởi người khác. Vui lòng chọn chu kỳ khác.");
                    } else {
                        // Hold cũ của chính user — gia hạn và tái sử dụng
                        existing.setHoldStartTime(extendedHoldStart);
                        temporaryBookingRepository.save(existing);
                        additionalHolds.add(existing);
                        continue;
                    }
                }

                TemporaryBooking newHold = new TemporaryBooking();
                newHold.setSubCourt(subCourt);
                newHold.setAvailableTime(time);
                newHold.setBookingDate(date);
                newHold.setUserId(user.getId());
                newHold.setHoldStartTime(extendedHoldStart);
                try {
                    additionalHolds.add(temporaryBookingRepository.saveAndFlush(newHold));
                } catch (DataIntegrityViolationException e) {
                    // Unique (sub_court, time, date) — race với user khác vừa insert
                    throw new IllegalArgumentException("Sân vào ngày "
                            + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            + " vừa bị người khác giữ chỗ. Vui lòng thử lại.");
                }
            }
        }

        // 8. Tính toán giá linh hoạt cho từng slot
        double totalBookingPrice = 0;
        List<PendingBookingData.SlotData> slots = new ArrayList<>();

        for (LocalDate date : datesToBook) {
            double basePrice = product.getPrice() - (product.getPrice() * product.getSale() / 100);
            BookingContext context = BookingContext.builder()
                    .user(user)
                    .time(time)
                    .bookingDate(date)
                    .build();
            double finalPriceForSlot = pricingService.calculateFinalPrice(basePrice, context);
            totalBookingPrice += finalPriceForSlot;
            slots.add(new PendingBookingData.SlotData(date, finalPriceForSlot, (long) product.getSale()));
        }

        double courtDeposit = product.getDepositPrice() * datesToBook.size();

        // 8b. Validate + tính giá vợt thuê kèm (chỉ cho ONE_TIME, đã check ở bước 2)
        List<PendingBookingData.RentalSlot> rentalSlots = new ArrayList<>();
        double rentalTotal = 0;
        if (hasRackets) {
            for (com.example.quanly.domain.dto.RentalItem item : rackets) {
                Racket racket = racketRepository.findById(item.getRacketId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Không tìm thấy vợt ID: " + item.getRacketId()));
                if (racket.getProduct() == null || racket.getProduct().getId() != productId) {
                    throw new IllegalArgumentException(
                            "Vợt \"" + racket.getName() + "\" không thuộc sân này.");
                }
                if (!racket.isAvailable()) {
                    throw new IllegalArgumentException(
                            "Vợt \"" + racket.getName() + "\" hiện không có sẵn.");
                }
                if (racket.getBookingStockQuantity() < item.getQuantity()) {
                    throw new IllegalArgumentException(
                            "Vợt \"" + racket.getName() + "\" chỉ còn "
                                    + racket.getBookingStockQuantity() + " chiếc, không đủ "
                                    + item.getQuantity() + " bạn yêu cầu.");
                }
                double unitPrice = racket.getRentalPricePerPlay();
                double subtotal = unitPrice * item.getQuantity();
                rentalSlots.add(new PendingBookingData.RentalSlot(racket, item.getQuantity(), unitPrice, subtotal));
                rentalTotal += subtotal;
            }
        }

        double depositPrice = courtDeposit + rentalTotal;
        double totalBookingFullPrice = totalBookingPrice + rentalTotal;

        // 9. Gia hạn hold để cover cửa sổ thanh toán VNPay.
        // Trick: TemporaryBooking.isExpired() = holdStartTime + 3 min < now (xem entity).
        // Để hold sống tới now + PAYMENT_HOLD_WINDOW (18 min), set holdStartTime vào tương lai
        // = now + (PAYMENT_HOLD_WINDOW - HOLD_EXPIRY_GRACE) = now + 15 min. Khi đó scheduled
        // cleaner sẽ không xoá hold cho tới đúng phút thứ 18.
        hold.setHoldStartTime(extendedHoldStart);
        temporaryBookingRepository.save(hold);

        // 10. Lưu vào cache — KHÔNG ghi DB
        List<Long> allHoldIds = new ArrayList<>();
        allHoldIds.add(hold.getId());
        for (TemporaryBooking h : additionalHolds) {
            allHoldIds.add(h.getId());
        }

        PendingBookingData data = new PendingBookingData(
                allHoldIds, user,
                receiverName, receiverAddress, receiverPhone,
                product, time, subCourt,
                bookingDate, type, recurringEndDate,
                totalBookingFullPrice, depositPrice, slots,
                rentalSlots);

        long pendingId = pendingBookingCache.store(data);
        return new PreparedBookingResult(pendingId, depositPrice);
    }

    /**
     * Called on successful VNPay deposit payment. Performs a final collision check then
     * writes the booking to DB with DA_DAT (deposit paid) status. Final settlement happens
     * at the venue — staff/admin manually flips status to DA_THANH_TOAN after the user pays the rest.
     */
    @Transactional
    public BookingResponseDTO confirmPendingBooking(PendingBookingData data) {
        // Final collision check — guard against a race where the hold expired
        for (PendingBookingData.SlotData slot : data.getSlots()) {
            Optional<BookingDetail> conflict = bookingDetailRepository
                    .findActiveBySubCourtAndAvailableTimeAndDate(data.getSubCourt(), data.getAvailableTime(), slot.getDate());
            if (conflict.isPresent()) {
                log.error("Xung đột lịch sau khi thanh toán thành công: subCourt={}, time={}, date={}",
                        data.getSubCourt().getId(), data.getAvailableTime().getId(), slot.getDate());
                throw new IllegalStateException("Sân đã bị đặt bởi người khác trong lúc thanh toán (ngày "
                        + slot.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ").");
            }
        }

        Booking booking = new Booking();
        booking.setUser(data.getUser());
        booking.setReceiverName(data.getReceiverName());
        booking.setReceiverAddress(data.getReceiverAddress());
        booking.setReceiverPhone(data.getReceiverPhone());
        booking.setAvailableTime(data.getAvailableTime());
        booking.setBookingDate(data.getFirstBookingDate());
        booking.setBookingType(data.getBookingType());
        booking.setRecurringEndDate(data.getRecurringEndDate());
        booking.setDepositPrice(data.getDepositPrice());
        booking.setTotalPrice(data.getTotalBookingPrice());
        booking.setStatus(BookingStatus.DA_DAT);

        Booking savedBooking = bookingRepository.save(booking);

        List<BookingDetail> details = new ArrayList<>();
        for (PendingBookingData.SlotData slot : data.getSlots()) {
            BookingDetail detail = new BookingDetail();
            detail.setBooking(savedBooking);
            detail.setProduct(data.getProduct());
            detail.setPrice(slot.getPrice());
            detail.setSubCourt(data.getSubCourt());
            detail.setDate(slot.getDate());
            detail.setSale(slot.getSale());
            detail.setAvailableTime(data.getAvailableTime());
            details.add(detail);
        }
        bookingDetailRepository.saveAll(details);

        // Tạo RentalTool cho từng vợt thuê kèm (nếu có)
        if (data.getRentals() != null && !data.getRentals().isEmpty()) {
            List<RentalTool> rentalTools = new ArrayList<>();
            for (PendingBookingData.RentalSlot rs : data.getRentals()) {
                Racket racket = rs.getRacket();
                RentalTool rt = new RentalTool();
                rt.setFullName(data.getReceiverName());
                rt.setEmail(data.getUser().getEmail());
                rt.setPhone(data.getReceiverPhone());
                rt.setType(RentalType.ON_SITE);
                rt.setBookingId(String.valueOf(savedBooking.getId()));
                rt.setRacketId(racket.getId());
                rt.setProductId(data.getProduct().getId());
                // Đồng bộ convention với flow DAILY cũ:
                // price = racket.price * quantity (giá trị vợt, tham chiếu)
                // rentalPrice = tổng tiền thuê thực tế user trả
                rt.setPrice(racket.getPrice() * rs.getQuantity());
                rt.setRentalPrice(rs.getSubtotal());
                rt.setStatus(RentalToolStatus.IN_USE);
                rt.setQuantity(rs.getQuantity());
                rt.setQuantityDay(1);
                rt.setRentalDate(data.getFirstBookingDate());
                rt.setReturnDate(data.getFirstBookingDate());
                rt.setCreateAt(LocalDateTime.now());
                rt.setUpdateAt(LocalDateTime.now());
                rt.setUserId(data.getUser().getId());
                rentalTools.add(rt);

                // Trừ stock
                racket.setBookingStockQuantity(racket.getBookingStockQuantity() - rs.getQuantity());
                racketRepository.save(racket);
            }
            rentalToolRepository.saveAll(rentalTools);

            // Đánh dấu booking có vợt đính kèm
            savedBooking.setRentalToolCode("BUNDLED");
            bookingRepository.save(savedBooking);
        }

        deleteAllHolds(data);

        return bookingMapper.toDTO(savedBooking);
    }

    /**
     * Called on failed VNPay payment. Frees the slot hold and removes the pending booking.
     */
    public void cancelPendingBooking(PendingBookingData data) {
        deleteAllHolds(data);
    }

    /**
     * Tính holdStartTime cần set để TemporaryBooking sống được {@code PAYMENT_HOLD_WINDOW}
     * kể từ {@code now}. Trick: vì {@code isExpired() = holdStartTime + 3min < now},
     * ta dịch holdStartTime ra tương lai để bù trừ.
     */
    private LocalDateTime computeExtendedHoldStart(LocalDateTime now) {
        return now.plus(PAYMENT_HOLD_WINDOW).minus(HOLD_EXPIRY_GRACE);
    }

    private void deleteAllHolds(PendingBookingData data) {
        List<Long> ids = data.getTemporaryBookingIds();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            if (id != null) {
                temporaryBookingRepository.deleteById(id);
            }
        }
    }

    @Transactional
    public Page<BookingResponseDTO> fetchBookingByUserWithPaging(Long userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable).map(bookingMapper::toDTO);
    }

    @Transactional
    public Page<BookingResponseDTO> fetchBookingByUserAndTypeWithPaging(Long userId, BookingType bookingType, Pageable pageable) {
        return bookingRepository.findByUserIdAndBookingType(userId, bookingType, pageable).map(bookingMapper::toDTO);
    }

    // =============================================================================
    // CANCEL BOOKING — user huỷ tự + admin xác nhận hoàn cọc (see CANCEL_BOOKING_FEATURE.md)
    // =============================================================================

    /** Kết quả tính toán refund — service layer trả về để controller build response. */
    public static class CancelResult {
        public final long bookingId;
        public final String bookingCode;
        public final double refundAmount;
        public final RefundStatus refundStatus;
        public final Integer usedSessions;   // null cho ONE_TIME
        public final Integer totalSessions;  // null cho ONE_TIME
        public CancelResult(long bookingId, String bookingCode, double refundAmount,
                            RefundStatus refundStatus, Integer usedSessions, Integer totalSessions) {
            this.bookingId = bookingId;
            this.bookingCode = bookingCode;
            this.refundAmount = refundAmount;
            this.refundStatus = refundStatus;
            this.usedSessions = usedSessions;
            this.totalSessions = totalSessions;
        }
    }

    @Transactional
    public CancelResult cancelByUser(long bookingId, long userId, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking id=" + bookingId));

        // Ownership
        if (booking.getUser() == null || booking.getUser().getId() != userId) {
            throw new IllegalArgumentException("Bạn không có quyền huỷ đơn này.");
        }

        // D0.1: không cho huỷ nếu đã hoàn thành / đã huỷ
        BookingStatus status = booking.getStatus();
        if (status == BookingStatus.DA_HUY) {
            throw new IllegalArgumentException("Đơn đã được huỷ trước đó.");
        }
        if (status == BookingStatus.DA_THANH_TOAN) {
            throw new IllegalArgumentException("Đơn đã hoàn thành (đã thanh toán đầy đủ), không thể huỷ.");
        }

        // Tính refund tùy loại
        double refund;
        Integer used = null;
        Integer total = null;
        RefundStatus refundStatus;

        if (booking.getBookingType() == BookingType.WEEKLY_RECURRING) {
            // D0.2: buổi "đã dùng" = BookingDetail.date < today
            LocalDate today = LocalDate.now();
            List<BookingDetail> details = bookingDetailRepository.findByBookingId(bookingId);
            total = details.size();
            used = (int) details.stream().filter(d -> d.getDate() != null && d.getDate().isBefore(today)).count();
            int remaining = total - used;
            // D0.3: refund = deposit × remaining / total
            if (total > 0 && remaining > 0) {
                refund = Math.round((booking.getDepositPrice() * remaining * 100.0) / total) / 100.0;
                refundStatus = RefundStatus.PENDING_REFUND;
            } else {
                refund = 0;
                refundStatus = RefundStatus.NOT_APPLICABLE;
            }
        } else {
            // ONE_TIME: check 2h gate
            LocalDateTime playStart = LocalDateTime.of(
                    booking.getBookingDate(),
                    booking.getAvailableTime() != null && booking.getAvailableTime().getTime() != null
                            ? booking.getAvailableTime().getTime()
                            : LocalTime.MIDNIGHT);
            LocalDateTime deadline = playStart.minusHours(2);
            if (LocalDateTime.now().isAfter(deadline)) {
                throw new IllegalArgumentException(
                        "Bạn không thể huỷ lịch đặt trong vòng 2 tiếng trước giờ bắt đầu.");
            }
            refund = booking.getDepositPrice();
            refundStatus = refund > 0 ? RefundStatus.PENDING_REFUND : RefundStatus.NOT_APPLICABLE;
        }

        // Update booking
        booking.setStatus(BookingStatus.DA_HUY);
        booking.setRefundStatus(refundStatus);
        booking.setRefundAmount(refund);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setUsedSessionsAtCancel(used);
        booking.setTotalSessionsAtCancel(total);
        booking.setCancelReason(reason);
        bookingRepository.save(booking);

        // D0.4: Cascade rental ON_SITE đính kèm
        List<RentalTool> attachedRentals = rentalToolRepository
                .findRentalToolsByBookingId(String.valueOf(bookingId));
        for (RentalTool rt : attachedRentals) {
            if (rt.getStatus() == RentalToolStatus.COMPLETED || rt.getStatus() == RentalToolStatus.CANCELLED) {
                continue;
            }
            rt.setStatus(RentalToolStatus.CANCELLED);
            rt.setUpdateAt(LocalDateTime.now());
            if (rt.getType() == RentalType.ON_SITE && rt.getRacketId() != null && rt.getQuantity() != null) {
                racketRepository.findById(rt.getRacketId()).ifPresent(r -> {
                    r.setBookingStockQuantity(r.getBookingStockQuantity() + rt.getQuantity());
                    racketRepository.save(r);
                });
            }
        }
        rentalToolRepository.saveAll(attachedRentals);

        log.info("Booking {} cancelled by user {}: refund={}, status={}", bookingId, userId, refund, refundStatus);

        // Phát SLOT_RELEASED cho từng BookingDetail còn trong tương lai để FE đang xem trang booking re-fresh
        try {
            LocalDate today = LocalDate.now();
            List<BookingDetail> details = bookingDetailRepository.findByBookingId(bookingId);
            for (BookingDetail bd : details) {
                if (bd.getDate() != null && !bd.getDate().isBefore(today)
                        && bd.getSubCourt() != null && bd.getAvailableTime() != null) {
                    slotEventPublisher.publishReleased(
                            bd.getSubCourt().getId(),
                            bd.getAvailableTime().getId(),
                            bd.getDate());
                }
            }
        } catch (Exception ex) {
            log.warn("Failed to publish SLOT_RELEASED on cancel: {}", ex.getMessage());
        }

        // Notification: gửi user + admin/staff
        try {
            String courtName = booking.getBookingDetails() != null && !booking.getBookingDetails().isEmpty()
                    && booking.getBookingDetails().get(0).getProduct() != null
                    ? booking.getBookingDetails().get(0).getProduct().getName()
                    : "Sân cầu lông";

            String userMsg;
            if (refundStatus == RefundStatus.PENDING_REFUND) {
                userMsg = String.format(
                        "Bạn đã huỷ đơn %s thành công. Yêu cầu hoàn cọc %,.0f VNĐ đã được gửi tới quản trị viên.",
                        booking.getBookingCode(), refund);
            } else {
                userMsg = String.format(
                        "Bạn đã huỷ đơn %s thành công. Không có khoản cọc nào được hoàn lại.",
                        booking.getBookingCode());
            }
            notificationService.sendToUser(userId, NotificationType.BOOKING_CANCELLED,
                    "BOOKING", bookingId,
                    "Huỷ đặt sân thành công", userMsg);

            if (refundStatus == RefundStatus.PENDING_REFUND) {
                String staffMsg = String.format(
                        "Người dùng vừa huỷ đơn %s tại %s. Cần hoàn cọc %,.0f VNĐ. SĐT: %s",
                        booking.getBookingCode(), courtName, refund,
                        booking.getReceiverPhone() != null ? booking.getReceiverPhone() : "(không có)");
                notificationService.sendToAdminStaff(NotificationType.REFUND_REQUEST,
                        "BOOKING", bookingId,
                        "Có yêu cầu hoàn cọc mới", staffMsg);
            }
        } catch (Exception ex) {
            log.warn("Failed to send notifications on cancel: {}", ex.getMessage());
        }

        return new CancelResult(booking.getId(), booking.getBookingCode(), refund, refundStatus, used, total);
    }

    @Transactional
    public Booking confirmRefund(long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking id=" + bookingId));

        if (booking.getStatus() != BookingStatus.DA_HUY) {
            throw new IllegalArgumentException("Booking chưa bị huỷ — không có gì để xác nhận hoàn cọc.");
        }
        if (booking.getRefundStatus() == RefundStatus.REFUNDED) {
            // Idempotent: đã refunded rồi thì không làm gì thêm
            return booking;
        }
        if (booking.getRefundStatus() != RefundStatus.PENDING_REFUND) {
            throw new IllegalArgumentException(
                    "Trạng thái hoàn cọc hiện tại không cho phép xác nhận: "
                            + (booking.getRefundStatus() != null ? booking.getRefundStatus().getLabel() : "không xác định"));
        }

        booking.setRefundStatus(RefundStatus.REFUNDED);
        bookingRepository.save(booking);
        log.info("Booking {} marked REFUNDED", bookingId);

        // Báo user biết cọc đã được hoàn
        try {
            if (booking.getUser() != null) {
                double amount = booking.getRefundAmount() != null ? booking.getRefundAmount() : 0;
                String msg = String.format(
                        "Cọc của đơn %s đã được hoàn (%,.0f VNĐ). Nếu chưa nhận được, vui lòng liên hệ admin.",
                        booking.getBookingCode(), amount);
                notificationService.sendToUser(booking.getUser().getId(), NotificationType.REFUND_DONE,
                        "BOOKING", bookingId,
                        "Đã hoàn cọc thành công", msg);
            }
        } catch (Exception ex) {
            log.warn("Failed to send REFUND_DONE notification: {}", ex.getMessage());
        }

        return booking;
    }
}
