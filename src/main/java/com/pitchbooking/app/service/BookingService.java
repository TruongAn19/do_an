package com.pitchbooking.app.service;

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
import com.pitchbooking.app.domain.dto.BookingResponseDTO;
import com.pitchbooking.app.domain.dto.PendingBookingData;
import com.pitchbooking.app.domain.dto.PreparedBookingResult;
import com.pitchbooking.app.mapper.BookingMapper;
import com.pitchbooking.app.repository.*;
import com.pitchbooking.app.service.pricing.BookingContext;
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
    UserRepository userRepository;
    ProductRepository productRepository;
    TimeRepository timeRepository;
    SubPitchRepository subPitchRepository;
    TemporaryBookingRepository temporaryBookingRepository;
    BookingMapper bookingMapper;
    PricingService pricingService;
    PendingBookingCache pendingBookingCache;

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

        // 2. Tính toán danh sách ngày cần đặt
        BookingType type = (bookingType != null) ? BookingType.valueOf(bookingType) : BookingType.ONE_TIME;
        List<LocalDate> datesToBook = new ArrayList<>();
        LocalDate finalEndDate = recurringEndDate;

        if (type == BookingType.WEEKLY_RECURRING) {
            if (durationMonths != null && durationMonths > 0) {
                finalEndDate = bookingDate.plusMonths(durationMonths);
            }

            if (finalEndDate == null) {
                throw new IllegalArgumentException("Thiếu thông tin thời hạn đặt sân cố định.");
            }

            if (finalEndDate.isBefore(bookingDate)) {
                throw new IllegalArgumentException("Ngày kết thúc chu kỳ không thể trước ngày bắt đầu.");
            }

            if (daysOfWeek == null || daysOfWeek.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn ít nhất một thứ trong tuần.");
            }

            // Duyệt từng ngày từ bookingDate đến finalEndDate
            LocalDate current = bookingDate;
            while (!current.isAfter(finalEndDate)) {
                // getValue() trả về 1 (Thứ 2) -> 7 (Chủ nhật)
                if (daysOfWeek.contains(current.getDayOfWeek().getValue())) {
                    datesToBook.add(current);
                }
                current = current.plusDays(1);
            }
        } else {
            datesToBook.add(bookingDate);
        }

        if (datesToBook.isEmpty()) {
            throw new IllegalArgumentException("Không có ngày nào hợp lệ trong khoảng thời gian đã chọn.");
        }

        // 3. Kiểm tra ngày đặt có hợp lệ (Tối đa 90 ngày)
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

        // 4. Lấy thông tin sân, khung giờ
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm ID: " + productId));
        SubPitch subPitch = subPitchRepository.findById(subPitchId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân phụ ID: " + subPitchId));
        AvailableTime time = timeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khung giờ ID: " + timeId));

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

        // 7. Tính toán giá và áp dụng chiết khấu
        double totalBookingPrice = 0;
        List<PendingBookingData.SlotData> slots = new ArrayList<>();
        double discountRate = pricingService.calculateRecurringDiscountRate(durationMonths);

        for (LocalDate date : datesToBook) {
            double basePrice = product.getPrice() - (product.getPrice() * product.getSale() / 100);
            BookingContext context = BookingContext.builder()
                    .user(user)
                    .time(time)
                    .bookingDate(date)
                    .build();
            double finalPriceForSlot = pricingService.calculateFinalPrice(basePrice, context);

            // Áp dụng chiết khấu thêm nếu là đặt định kỳ
            if (type == BookingType.WEEKLY_RECURRING) {
                finalPriceForSlot = finalPriceForSlot * (1 - discountRate);
            }

            totalBookingPrice += finalPriceForSlot;
            slots.add(new PendingBookingData.SlotData(date, finalPriceForSlot, (long) product.getSale()));
        }

        double depositPrice = product.getDepositPrice() * datesToBook.size();
        // Cọc cũng được giảm tương ứng để hỗ trợ khách
        if (type == BookingType.WEEKLY_RECURRING) {
            depositPrice = depositPrice * (1 - discountRate);
        }

        // 8. Gia hạn giữ chỗ
        hold.setHoldStartTime(LocalDateTime.now().plusMinutes(15));
        temporaryBookingRepository.save(hold);

        // 9. Lưu vào cache (chỉ ID + snapshot field cần thiết, không phải entity refs)
        PendingBookingData data = new PendingBookingData(
                hold.getId(),
                user.getId(), user.getEmail(),
                receiverName, receiverAddress, receiverPhone,
                product.getId(), time.getId(), subPitch.getId(),
                bookingDate, type, finalEndDate,
                daysOfWeek, durationMonths,
                totalBookingPrice, depositPrice, slots);

        long pendingId = pendingBookingCache.store(data);
        return new PreparedBookingResult(pendingId, depositPrice);
    }

    /**
     * Called on successful VNPay payment. Performs a final collision check then
     * writes the confirmed booking to DB with DA_THANH_TOAN status.
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

    @Transactional
    public void cancelBooking(long bookingId, User user) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt sân ID: " + bookingId));

        if (booking.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("Bạn không có quyền hủy đơn đặt sân này.");
        }

        if (booking.getStatus() == BookingStatus.DA_HUY) {
            throw new IllegalArgumentException("Đơn đặt sân này đã được hủy trước đó.");
        }

        if (booking.getStatus() == BookingStatus.DA_THANH_TOAN) {
            throw new IllegalArgumentException("Đơn đặt sân đã hoàn tất thanh toán, không thể tự hủy qua hệ thống. Vui lòng liên hệ quản lý.");
        }

        if (booking.getBookingDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Không thể hủy đơn đặt sân đã qua hoặc đang trong ngày thi đấu.");
        }

        // 1. Giải phóng các slot (xóa BookingDetail để người khác có thể đặt)
        List<BookingDetail> details = booking.getBookingDetails();
        if (details != null && !details.isEmpty()) {
            bookingDetailRepository.deleteAllInBatch(details);
        }

        // 2. Cập nhật trạng thái
        booking.setStatus(BookingStatus.DA_HUY);
        bookingRepository.save(booking);

        log.info("Client {} cancelled booking ID: {}", user.getEmail(), bookingId);
    }
}
