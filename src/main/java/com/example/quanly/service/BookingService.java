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
    SubCourtRepository subCourtRepository;
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
            long productId, long timeId, long subCourtId, LocalDate bookingDate,
            String bookingType, LocalDate recurringEndDate) {

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

        // 9. Mở rộng thời gian giữ chỗ để đủ thời gian thanh toán VNPay (~18 phút từ lúc này)
        hold.setHoldStartTime(LocalDateTime.now().plusMinutes(15));
        temporaryBookingRepository.save(hold);

        // 10. Lưu vào cache — KHÔNG ghi DB
        PendingBookingData data = new PendingBookingData(
                hold.getId(), user,
                receiverName, receiverAddress, receiverPhone,
                product, time, subCourt,
                bookingDate, type, recurringEndDate,
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
}
