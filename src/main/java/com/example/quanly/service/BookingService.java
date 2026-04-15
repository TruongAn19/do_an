package com.example.quanly.service;

import com.example.quanly.domain.*;
import com.example.quanly.domain.dto.BookingResponseDTO;
import com.example.quanly.mapper.BookingMapper;
import com.example.quanly.repository.*;
import com.example.quanly.service.pricing.BookingContext;
import com.example.quanly.service.pricing.PricingService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    EmailService emailService;
    BookingMapper bookingMapper;
    PricingService pricingService;

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

    public void deleteBookingById(long id) {
        // delete order detail
        Optional<Booking> bookingOptional = this.bookingRepository.findById(id);
        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();
            List<BookingDetail> bookingDetails = booking.getBookingDetails();
            for (BookingDetail bookingDetail : bookingDetails) {
                this.bookingDetailRepository.deleteById(bookingDetail.getId());
            }
        }

        this.bookingRepository.deleteById(id);
    }

    public void updateBooking(long id, String status) {
        Optional<Booking> bOptional = this.bookingRepository.findById(id);
        if (bOptional.isEmpty())
            return;
        Booking currentBooking = bOptional.get();
        currentBooking.setStatus(status);
        this.bookingRepository.save(currentBooking);
        if (currentBooking.getStatus().equals("Đã thanh toán")) {
            List<RentalTool> rentalTools = rentalToolRepository.findRentalToolsByBookingId(currentBooking.getId() + "");
            for (RentalTool rentalTool : rentalTools) {
                rentalTool.setStatus(RentalToolStatus.COMPLETED);
                rentalToolRepository.save(rentalTool);
            }
        }

    }

    public List<BookingResponseDTO> fetchBookingByUser(User user) {
        return this.bookingRepository.findByUser(user).stream()
                .map(bookingMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingResponseDTO handlePlaceBooking(User user,
            String receiverName, String receiverAddress, String receiverPhone,
            long productId, long timeId, long subCourtId, LocalDate bookingDate,
            String bookingType, LocalDate recurringEndDate) {

        // 1. Kiểm tra người dùng
        user = userRepository.findById(user.getId());
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng.");
        }

        // 2. Tính toán danh sách ngày cần đặt
        java.util.List<LocalDate> datesToBook = new java.util.ArrayList<>();
        if ("WEEKLY_RECURRING".equals(bookingType) && recurringEndDate != null) {
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
        Product product = productRepository.findById(productId).orElse(null);
        SubCourt subCourt = subCourtRepository.findById(subCourtId).orElse(null);
        AvailableTime time = timeRepository.findById(timeId).orElse(null);

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

        // 8. Tạo Header Booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setReceiverName(receiverName);
        booking.setReceiverAddress(receiverAddress);
        booking.setReceiverPhone(receiverPhone);
        booking.setAvailableTime(time);
        booking.setBookingDate(bookingDate);
        booking.setBookingType(bookingType != null ? bookingType : "ONE_TIME");
        booking.setRecurringEndDate(recurringEndDate);
        booking.setDepositPrice(product.getDepositPrice() * datesToBook.size()); // Cọc nhân lên
        booking.setStatus("Đã đặt");

        // 9. Tính toán giá linh hoạt cho từng slot và cộng dồn
        double totalBookingPrice = 0;
        java.util.List<BookingDetail> details = new java.util.ArrayList<>();

        for (LocalDate date : datesToBook) {
            double basePrice = product.getPrice() - (product.getPrice() * product.getSale() / 100);
            BookingContext context = BookingContext.builder()
                    .user(user)
                    .time(time)
                    .bookingDate(date)
                    .build();

            double finalPriceForSlot = pricingService.calculateFinalPrice(basePrice, context);
            totalBookingPrice += finalPriceForSlot;

            BookingDetail detail = new BookingDetail();
            detail.setBooking(booking);
            detail.setProduct(product);
            detail.setPrice(finalPriceForSlot);
            detail.setSubCourt(subCourt);
            detail.setDate(date);
            detail.setSale(product.getSale());
            detail.setAvailableTime(time);
            details.add(detail);
        }

        booking.setTotalPrice(totalBookingPrice);
        Booking savedBooking = bookingRepository.save(booking);

        for (BookingDetail detail : details) {
            bookingDetailRepository.save(detail);
        }

        // Xóa giữ chỗ
        temporaryBookingRepository.delete(hold);

        emailService.sendBookingConfirmationEmail(user.getEmail(), savedBooking.getBookingCode(), booking.getId());
        return bookingMapper.toDTO(savedBooking);
    }

    public Page<BookingResponseDTO> fetchBookingByUserWithPaging(Long userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable).map(bookingMapper::toDTO);
    }

}
