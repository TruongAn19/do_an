package com.example.quanly.service;

import com.example.quanly.config.ContactInfo;
import com.example.quanly.domain.*;
import com.example.quanly.domain.dto.BookingResponseDTO;
import com.example.quanly.domain.dto.CancelBookingResponse;
import com.example.quanly.domain.dto.NotificationDTO;
import com.example.quanly.domain.dto.PendingBookingData;
import com.example.quanly.domain.dto.PreparedBookingResult;
import com.example.quanly.domain.dto.RentalItem;
import com.example.quanly.mapper.BookingMapper;
import com.example.quanly.repository.*;
import com.example.quanly.service.pricing.BookingContext;
import com.example.quanly.service.pricing.PricingService;
import com.example.quanly.exception.BusinessConflictException;
import com.example.quanly.exception.ForbiddenOperationException;
import com.example.quanly.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    RacketRepository racketRepository;
    UserRepository userRepository;
    ProductRepository productRepository;
    TimeRepository timeRepository;
    SubCourtRepository subCourtRepository;
    SubCourtAvailableTimeRepository subCourtAvailableTimeRepository;
    TemporaryBookingRepository temporaryBookingRepository;
    BookingMapper bookingMapper;
    PricingService pricingService;
    PendingBookingCache pendingBookingCache;
    NotificationService notificationService;
    ContactInfo contactInfo;

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

    public Optional<BookingResponseDTO> fetchBookingByIdAndUser(Long id, Long userId) {
        return bookingRepository.findByIdAndUserId(id, userId).map(bookingMapper::toDTO);
    }

    @Transactional
    public void deleteBookingById(long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking id=" + id));
        if (booking.getStatus() == BookingStatus.DA_HUY) {
            return;
        }
        if (booking.getUser() == null) {
            throw new BusinessConflictException("Booking không có người dùng để thực hiện hủy an toàn.");
        }
        cancelByUser(id, booking.getUser().getId(), "Được hủy bởi quản trị viên");
    }

    @Transactional
    public void updateBooking(long id, String status) {
        Booking currentBooking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking id=" + id));
        BookingStatus currentStatus = currentBooking.getStatus();
        BookingStatus targetStatus = BookingStatus.fromLabel(status);

        if (currentStatus == targetStatus) {
            return;
        }
        if (!isAllowedStatusTransition(currentStatus, targetStatus)) {
            throw new BusinessConflictException(
                    "Không thể chuyển trạng thái booking từ " + currentStatus + " sang " + targetStatus + ".");
        }

        if (targetStatus == BookingStatus.DA_THANH_TOAN) {
            completeOnSiteRentals(currentBooking.getId());
        }
        currentBooking.setStatus(targetStatus);
        bookingRepository.save(currentBooking);
    }

    private boolean isAllowedStatusTransition(BookingStatus current, BookingStatus target) {
        if (current == null || target == BookingStatus.DA_HUY) {
            return false;
        }
        return switch (current) {
            case CHO_THANH_TOAN -> target == BookingStatus.DA_DAT
                    || target == BookingStatus.DA_DAT_COC;
            case DA_DAT -> target == BookingStatus.DA_DAT_COC
                    || target == BookingStatus.DA_THANH_TOAN;
            case DA_DAT_COC -> target == BookingStatus.DA_THANH_TOAN;
            case DA_THANH_TOAN, DA_HUY -> false;
        };
    }

    private void completeOnSiteRentals(long bookingId) {
        List<RentalTool> rentalTools = rentalToolRepository.findRentalToolsByBookingId(String.valueOf(bookingId));
        LocalDateTime now = LocalDateTime.now();
        for (RentalTool rental : rentalTools) {
            if (rental.getType() != RentalType.ON_SITE
                    || (rental.getStatus() != RentalToolStatus.PAID
                    && rental.getStatus() != RentalToolStatus.RENTING)) {
                continue;
            }
            Racket racket = racketRepository.findByIdForUpdate(rental.getRacketId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy vợt id=" + rental.getRacketId()));
            racket.setBookingStockQuantity(racket.getBookingStockQuantity() + rental.getQuantity());
            racketRepository.save(racket);
            rental.setStatus(RentalToolStatus.COMPLETED);
            rental.setUpdateAt(now);
            rentalToolRepository.save(rental);
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
     * Validates and pre-computes booking data, persists the pending payment session,
     * and extends the TemporaryBooking hold to cover the VNPay payment window.
     */
    @Transactional
    public PreparedBookingResult preparePendingBooking(User user,
            String receiverName, String receiverAddress, String receiverPhone,
            long productId, long timeId, long subCourtId, LocalDate bookingDate,
            String bookingType, LocalDate recurringEndDate, List<RentalItem> rackets) {

        // 1. Kiểm tra người dùng
        user = userRepository.findUserById(user.getId());
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng.");
        }

        // 2. Tính toán danh sách ngày cần đặt
        BookingType type = (bookingType != null) ? BookingType.valueOf(bookingType) : BookingType.ONE_TIME;
        List<LocalDate> datesToBook = new ArrayList<>();
        if (type == BookingType.WEEKLY_RECURRING && recurringEndDate != null) {
            if (recurringEndDate.isBefore(bookingDate)) {
                throw new IllegalArgumentException("Ngày kết thúc chu kỳ không thể trước ngày bắt đầu.");
            }
            LocalDate nextDate = bookingDate;
            while (!nextDate.isAfter(recurringEndDate)) {
                datesToBook.add(nextDate);
                nextDate = nextDate.plusWeeks(1);
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
        if (subCourt.getProduct() == null || !Objects.equals(subCourt.getProduct().getId(), product.getId())) {
            throw new IllegalArgumentException("Sân phụ không thuộc sân đã chọn.");
        }
        if (subCourtAvailableTimeRepository.findBySubCourtAndAvailableTime(subCourt, time).isEmpty()) {
            throw new IllegalArgumentException("Khung giờ không được cấu hình cho sân phụ đã chọn.");
        }

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
                    .findBySubCourtAndAvailableTimeAndDate(subCourt, time, date);
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

        // 8. Tính toán giá linh hoạt cho từng slot + chiết khấu đặt sân tháng
        List<TemporaryBooking> holds = holdRecurringSlots(
                user.getId(), subCourt, time, datesToBook, hold);

        double totalBookingPrice = 0;
        List<PendingBookingData.SlotData> slots = new ArrayList<>();

        // Xác định chiết khấu dựa trên số tháng (nếu là đặt định kỳ)
        double recurringDiscountRate = 0;
        if (type == BookingType.WEEKLY_RECURRING && recurringEndDate != null) {
            long months = java.time.temporal.ChronoUnit.MONTHS.between(bookingDate.withDayOfMonth(1), recurringEndDate.withDayOfMonth(1));
            if (months <= 1) recurringDiscountRate = 5;
            else if (months == 2) recurringDiscountRate = 8;
            else recurringDiscountRate = 10;
        }

        for (LocalDate date : datesToBook) {
            double basePrice = product.getPrice() - (product.getPrice() * product.getSale() / 100);
            
            // Áp dụng thêm chiết khấu đặt sân tháng
            if (recurringDiscountRate > 0) {
                basePrice = basePrice - (basePrice * recurringDiscountRate / 100);
            }

            BookingContext context = BookingContext.builder()
                    .user(user)
                    .time(time)
                    .bookingDate(date)
                    .build();
            double finalPriceForSlot = pricingService.calculateFinalPrice(basePrice, context);
            totalBookingPrice += finalPriceForSlot;
            slots.add(new PendingBookingData.SlotData(date, finalPriceForSlot, (long) (product.getSale() + recurringDiscountRate)));
        }

        double depositPrice = product.getDepositPrice() * datesToBook.size();
        // Áp dụng chiết khấu cả vào tiền cọc (tùy chọn, nhưng thường là cọc theo % tổng)
        if (recurringDiscountRate > 0) {
            depositPrice = depositPrice - (depositPrice * recurringDiscountRate / 100);
        }

        // 8b. Bundled rental — thuê vợt kèm theo booking (chỉ ONE_TIME)
        List<PendingBookingData.RentalSlot> rentalSlots = new ArrayList<>();
        double rentalTotal = 0;
        if (rackets != null && !rackets.isEmpty()) {
            if (type == BookingType.WEEKLY_RECURRING) {
                throw new IllegalArgumentException("Không hỗ trợ thuê vợt cho đặt sân theo tháng.");
            }
            for (RentalItem item : rackets) {
                if (item == null || item.getRacketId() == null || item.getQuantity() <= 0) {
                    continue;
                }
                Racket racket = racketRepository.findById(item.getRacketId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Không tìm thấy vợt ID: " + item.getRacketId()));
                if (racket.getBookingStockQuantity() < item.getQuantity()) {
                    throw new IllegalArgumentException("Vợt " + racket.getName()
                            + " không đủ số lượng. Còn lại: " + racket.getBookingStockQuantity() + ".");
                }
                double unitPrice = racket.getRentalPricePerPlay();
                double subtotal = unitPrice * item.getQuantity();
                rentalTotal += subtotal;
                rentalSlots.add(new PendingBookingData.RentalSlot(
                        racket.getId(), item.getQuantity(), unitPrice, subtotal));
            }
        }

        // Luồng thuê kèm sân thu ngay cọc sân + phí thuê vợt qua VNPay.
        depositPrice += rentalTotal;
        totalBookingPrice += rentalTotal;

        // 9. Mở rộng thời gian giữ chỗ để đủ thời gian thanh toán VNPay (~18 phút từ lúc này)
        LocalDateTime paymentExpiry = LocalDateTime.now().plusMinutes(18);
        holds.forEach(currentHold -> currentHold.setExpiresAt(paymentExpiry));
        temporaryBookingRepository.saveAll(holds);

        // 10. Lưu phiên thanh toán bền vững trong DB, có TTL
        PendingBookingData data = new PendingBookingData(
                hold.getId(), user,
                receiverName, receiverAddress, receiverPhone,
                product, time, subCourt,
                bookingDate, type, recurringEndDate,
                totalBookingPrice, depositPrice, slots, rentalSlots,
                holds.stream().map(TemporaryBooking::getId).toList());

        long pendingId = pendingBookingCache.store(data);
        return new PreparedBookingResult(pendingId, depositPrice);
    }

    private List<TemporaryBooking> holdRecurringSlots(
            Long userId,
            SubCourt subCourt,
            AvailableTime time,
            List<LocalDate> datesToBook,
            TemporaryBooking firstHold) {
        List<TemporaryBooking> holds = new ArrayList<>();
        holds.add(firstHold);
        for (int i = 1; i < datesToBook.size(); i++) {
            LocalDate date = datesToBook.get(i);
            Optional<TemporaryBooking> existing = temporaryBookingRepository
                    .findBySubCourtAndAvailableTimeAndBookingDateWithLock(subCourt, time, date);
            TemporaryBooking currentHold;
            if (existing.isPresent() && !existing.get().isExpired()) {
                currentHold = existing.get();
                if (!currentHold.getUserId().equals(userId)) {
                    throw new BusinessConflictException(
                            "Sân đang được người khác giữ vào ngày "
                                    + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".");
                }
            } else {
                existing.ifPresent(temporaryBookingRepository::delete);
                temporaryBookingRepository.flush();
                currentHold = new TemporaryBooking();
                currentHold.setSubCourt(subCourt);
                currentHold.setAvailableTime(time);
                currentHold.setBookingDate(date);
                currentHold.setUserId(userId);
                currentHold.setHoldStartTime(LocalDateTime.now());
                currentHold.setExpiresAt(LocalDateTime.now().plusMinutes(18));
                currentHold = temporaryBookingRepository.saveAndFlush(currentHold);
            }
            holds.add(currentHold);
        }
        return holds;
    }

    /**
     * Called on successful VNPay payment. Performs a final collision check then
     * writes the confirmed booking to DB with DA_DAT_COC status.
     */
    @Transactional
    public BookingResponseDTO confirmPendingBooking(PendingBookingData data) {
        // Final collision check — guard against a race where the hold expired
        for (PendingBookingData.SlotData slot : data.getSlots()) {
            Optional<BookingDetail> conflict = bookingDetailRepository
                    .findBySubCourtAndAvailableTimeAndDate(data.getSubCourt(), data.getAvailableTime(), slot.getDate());
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
        booking.setStatus(BookingStatus.DA_DAT_COC);

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
            detail.setSlotActive(Boolean.TRUE);
            details.add(detail);
        }
        try {
            bookingDetailRepository.saveAllAndFlush(details);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "Sân đã bị đặt bởi người khác trong lúc thanh toán.", e);
        }

        // Tạo RentalTool cho vợt thuê kèm (bundled rental) + trừ stock
        List<PendingBookingData.RentalSlot> rentalSlots = data.getRentalSlots();
        if (rentalSlots != null && !rentalSlots.isEmpty()) {
            LocalDateTime nowTs = LocalDateTime.now();
            for (PendingBookingData.RentalSlot slot : rentalSlots) {
                Racket racket = racketRepository.findByIdForUpdate(slot.getRacketId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Không tìm thấy vợt ID: " + slot.getRacketId()));
                if (racket.getBookingStockQuantity() < slot.getQuantity()) {
                    throw new IllegalStateException("Vợt " + racket.getName()
                            + " không đủ số lượng trong lúc thanh toán. Còn lại: "
                            + racket.getBookingStockQuantity() + ".");
                }

                RentalTool tool = new RentalTool();
                tool.setType(RentalType.ON_SITE);
                tool.setBookingId(String.valueOf(savedBooking.getId()));
                tool.setStatus(RentalToolStatus.PAID);
                tool.setQuantityDay(1);
                tool.setQuantity(slot.getQuantity());
                tool.setRacketId(slot.getRacketId());
                tool.setProductId(data.getProduct().getId());
                tool.setUserId(data.getUser().getId());
                tool.setPrice(slot.getUnitPrice() * slot.getQuantity());
                tool.setRentalPrice(slot.getSubtotal());
                tool.setFullName(data.getReceiverName());
                tool.setPhone(data.getReceiverPhone());
                tool.setEmail(data.getUser().getEmail());
                tool.setRentalDate(savedBooking.getBookingDate());
                tool.setCreateAt(nowTs);
                tool.setUpdateAt(nowTs);
                rentalToolRepository.save(tool);

                racket.setBookingStockQuantity(racket.getBookingStockQuantity() - slot.getQuantity());
                racketRepository.save(racket);
            }
            savedBooking.setRentalToolCode("BUNDLED");
            bookingRepository.save(savedBooking);
        }

        releaseTemporaryBookings(data);

        return bookingMapper.toDTO(savedBooking);
    }

    /**
     * Called on failed VNPay payment. Frees the slot hold and removes the pending booking.
     */
    public void cancelPendingBooking(PendingBookingData data) {
        releaseTemporaryBookings(data);
    }

    private void releaseTemporaryBookings(PendingBookingData data) {
        List<Long> holdIds = data.getTemporaryBookingIds();
        if (holdIds != null && !holdIds.isEmpty()) {
            temporaryBookingRepository.deleteAllByIdInBatch(holdIds);
        } else if (data.getTemporaryBookingId() != null) {
            temporaryBookingRepository.deleteById(data.getTemporaryBookingId());
        }
    }

    @Transactional
    public Page<BookingResponseDTO> fetchBookingByUserWithPaging(Long userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable).map(bookingMapper::toDTO);
    }

    // ============================ Nhóm 2: Cancel + Refund ============================

    /**
     * User tự huỷ booking. Tính tiền hoàn theo loại đặt (ONE_TIME có gate 2h,
     * WEEKLY theo tỉ lệ buổi còn lại), cascade huỷ rental ON_SITE đính kèm + trả stock,
     * và tạo notification cho user + staff/admin.
     */
    @Transactional
    public CancelBookingResponse cancelByUser(Long bookingId, Long userId, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking id=" + bookingId));

        // 1. Verify ownership
        if (booking.getUser() == null || booking.getUser().getId() != userId) {
            throw new ForbiddenOperationException("Bạn không có quyền huỷ đơn đặt sân này.");
        }

        // 2. Kiểm tra trạng thái
        BookingStatus status = booking.getStatus();
        if (status == BookingStatus.DA_HUY) {
            throw new BusinessConflictException("Booking đã được huỷ.");
        }
        if (status == BookingStatus.DA_THANH_TOAN) {
            throw new BusinessConflictException("Đơn đã hoàn thành, không thể huỷ.");
        }
        if (!(status == BookingStatus.CHO_THANH_TOAN
                || status == BookingStatus.DA_DAT
                || status == BookingStatus.DA_DAT_COC)) {
            throw new BusinessConflictException("Trạng thái đơn không cho phép huỷ.");
        }

        // 3. Tính refund
        LocalDate today = LocalDate.now();
        List<BookingDetail> details = booking.getBookingDetails() != null
                ? booking.getBookingDetails() : new ArrayList<>();
        boolean oneTime = booking.getBookingType() == null
                || booking.getBookingType() == BookingType.ONE_TIME;

        double refund;
        int totalSessions;
        int usedSessions;

        if (oneTime) {
            totalSessions = 1;
            usedSessions = 0;
            LocalTime time = booking.getAvailableTime() != null
                    ? booking.getAvailableTime().getTime() : LocalTime.MIN;
            LocalDateTime startDateTime = booking.getBookingDate().atTime(time);
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(startDateTime.minusHours(2))) {
                long minutesToStart = java.time.Duration.between(now, startDateTime).toMinutes();
                String msg = minutesToStart >= 0
                        ? "Không thể huỷ trong vòng 2 tiếng trước giờ bắt đầu (còn " + minutesToStart + " phút)."
                        : "Không thể huỷ vì đã qua giờ bắt đầu.";
                throw new BusinessConflictException(msg);
            }
            refund = booking.getDepositPrice();
        } else {
            totalSessions = details.size();
            usedSessions = (int) details.stream()
                    .filter(d -> d.getDate() != null && d.getDate().isBefore(today))
                    .count();
            int remaining = totalSessions - usedSessions;
            refund = totalSessions > 0 ? booking.getDepositPrice() * remaining / totalSessions : 0;
        }

        // 4. Cập nhật booking
        RefundStatus refundStatus = refund > 0 ? RefundStatus.PENDING_REFUND : RefundStatus.NOT_APPLICABLE;
        booking.setStatus(BookingStatus.DA_HUY);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setUsedSessionsAtCancel(usedSessions);
        booking.setTotalSessionsAtCancel(totalSessions);
        booking.setCancelReason(reason);
        booking.setRefundAmount(refund);
        booking.setRefundStatus(refundStatus);
        bookingRepository.save(booking);

        // Giải phóng unique slot key nhưng vẫn giữ BookingDetail để phục vụ lịch sử/refund.
        details.forEach(detail -> detail.setSlotActive(null));
        bookingDetailRepository.saveAll(details);

        // 5. Cascade rental ON_SITE đính kèm → CANCELLED + trả stock
        List<RentalTool> tools = rentalToolRepository.findRentalToolsByBookingId(String.valueOf(bookingId));
        for (RentalTool rt : tools) {
            if (rt.getStatus() == RentalToolStatus.CANCELLED) continue;
            rt.setStatus(RentalToolStatus.CANCELLED);
            rt.setUpdateAt(LocalDateTime.now());
            rentalToolRepository.save(rt);
            if (rt.getRacketId() != null && rt.getQuantity() != null) {
                racketRepository.findByIdForUpdate(rt.getRacketId()).ifPresent(racket -> {
                    racket.setBookingStockQuantity(racket.getBookingStockQuantity() + rt.getQuantity());
                    racketRepository.save(racket);
                });
            }
        }

        // 6. Notifications
        String userMsg = "Đơn đặt sân " + booking.getBookingCode() + " đã được huỷ. "
                + (refund > 0
                        ? "Số tiền hoàn dự kiến: " + formatVnd(refund) + ". Chúng tôi sẽ hoàn cọc sớm nhất."
                        : "Không có cọc được hoàn.");
        NotificationDTO userDto = NotificationDTO.from(notificationService.create(
                userId, NotificationType.BOOKING_CANCELLED, "BOOKING", booking.getId(),
                "Huỷ đặt sân thành công", userMsg));
        notificationService.pushToUser(booking.getUser().getEmail(), userDto);

        // Staff/admin chỉ cần biết khi có tiền hoàn (E11)
        if (refund > 0) {
            String staffTitle = "Yêu cầu hoàn cọc mới";
            String staffMsg = "Booking " + booking.getBookingCode() + " (" + booking.getReceiverName()
                    + ") đã huỷ, cần hoàn cọc " + formatVnd(refund) + ".";
            NotificationDTO lastDto = null;
            for (Long sid : notificationService.staffAndAdminUserIds()) {
                lastDto = NotificationDTO.from(notificationService.create(
                        sid, NotificationType.REFUND_REQUEST, "BOOKING", booking.getId(), staffTitle, staffMsg));
            }
            notificationService.pushToStaff(lastDto != null ? lastDto : NotificationDTO.builder()
                    .type(NotificationType.REFUND_REQUEST.name()).refType("BOOKING").refId(booking.getId())
                    .title(staffTitle).message(staffMsg).isRead(false).createdAt(LocalDateTime.now()).build());
        }

        return CancelBookingResponse.builder()
                .refundAmount(refund)
                .refundStatus(refundStatus.name())
                .usedSessions(usedSessions)
                .totalSessions(totalSessions)
                .hotline(contactInfo.getHotline())
                .email(contactInfo.getEmail())
                .build();
    }

    /**
     * Admin/staff xác nhận đã chuyển khoản hoàn cọc cho user. Idempotent nếu đã REFUNDED.
     */
    @Transactional
    public void confirmRefund(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking id=" + bookingId));

        if (booking.getStatus() != BookingStatus.DA_HUY) {
            throw new BusinessConflictException("Chỉ có thể hoàn cọc cho đơn đã huỷ.");
        }
        if (booking.getRefundStatus() == RefundStatus.REFUNDED) {
            return; // idempotent
        }
        if (booking.getRefundStatus() != RefundStatus.PENDING_REFUND) {
            throw new BusinessConflictException("Đơn này không ở trạng thái chờ hoàn cọc.");
        }

        booking.setRefundStatus(RefundStatus.REFUNDED);
        bookingRepository.save(booking);

        String msg = "Cọc đơn " + booking.getBookingCode() + " đã được hoàn"
                + (booking.getRefundAmount() != null ? " (" + formatVnd(booking.getRefundAmount()) + ")" : "")
                + ". Vui lòng kiểm tra tài khoản.";
        if (booking.getUser() != null) {
            NotificationDTO dto = NotificationDTO.from(notificationService.create(
                    booking.getUser().getId(), NotificationType.REFUND_DONE, "BOOKING", booking.getId(),
                    "Đã hoàn cọc", msg));
            notificationService.pushToUser(booking.getUser().getEmail(), dto);
        }
    }

    private String formatVnd(double amount) {
        return String.format("%,.0f VNĐ", amount);
    }

    @Transactional
    public Page<BookingResponseDTO> fetchRefundRequests(String refundStatus, Pageable pageable) {
        if (refundStatus != null && !refundStatus.isBlank()) {
            RefundStatus rs = RefundStatus.valueOf(refundStatus);
            return bookingRepository.findByRefundStatus(rs, pageable).map(bookingMapper::toDTO);
        }
        // "Tất cả" — mọi booking đã huỷ
        return bookingRepository.findByStatus(BookingStatus.DA_HUY, pageable).map(bookingMapper::toDTO);
    }
}
