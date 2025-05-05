package com.example.quanly.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.example.quanly.domain.*;
import com.example.quanly.repository.*;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;


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



    public List<Booking> fetchAllBookings() {
        return this.bookingRepository.findAll();
    }

    public Optional<Booking> fetchBookingById(long id) {
        return this.bookingRepository.findById(id);
    }

    public void deleteBookingById(long id) {
        // delete order detail
        Optional<Booking> bookingOptional = this.fetchBookingById(id);
        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();
            List<BookingDetail> bookingDetails = booking.getBookingDetails();
            for (BookingDetail bookingDetail : bookingDetails) {
                this.bookingDetailRepository.deleteById(bookingDetail.getId());
            }
        }

        this.bookingRepository.deleteById(id);
    }

    public void updateBooking(Booking booking) {
        Optional<Booking> bOptional = this.fetchBookingById(booking.getId());
        if (bOptional.isEmpty())
            return;
        Booking currentBooking = bOptional.get();
        currentBooking.setStatus(booking.getStatus());
        this.bookingRepository.save(currentBooking);
        if (currentBooking.getStatus().equals("Đã thanh toán")) {
            List<RentalTool> rentalTools = rentalToolRepository.findRentalToolsByBookingId(currentBooking.getId() + "");
            for (RentalTool rentalTool : rentalTools) {
                rentalTool.setStatus(RentalToolStatus.COMPLETED);
                rentalToolRepository.save(rentalTool);
            }
        }

    }

    public List<Booking> fetchBookingByUser(User user) {
        return this.bookingRepository.findByUser(user);
    }

    public List<Booking> fetchBookingsByDate(LocalDate date) {
        return bookingRepository.findByBookingDetailsDate(date);
    }

    public List<Booking> fetchBookingCode(String bookingCode) {
        return  bookingRepository.findByBookingCodeContainingIgnoreCase(bookingCode);
    }

    public void handlePlaceBooking(User user, HttpSession session,
                                   String receiverName, String receiverAddress, String receiverPhone,
                                   long productId, long timeId, long subCourtId, LocalDate bookingDate) {

        // 1. Kiểm tra người dùng
        user = userRepository.findById(user.getId());
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng.");
        }

        // 2. Kiểm tra ngày đặt
        LocalDate today = LocalDate.now();
        if (bookingDate.isBefore(today)) {
            throw new IllegalArgumentException("Không thể đặt sân cho ngày trong quá khứ.");
        }

        // 3. Lấy thông tin sân, khung giờ
        Product product = productRepository.getById(productId);
        SubCourt subCourt = subCourtRepository.getById(subCourtId);
        AvailableTime time = timeRepository.getById(timeId);

        // 4. Nếu đặt cho ngày hôm nay, kiểm tra khung giờ
        if (bookingDate.equals(today)) {
            LocalTime currentTime = LocalTime.now();
            if (time.getTime().isBefore(currentTime)) {
                throw new IllegalArgumentException("Khung giờ đã qua, vui lòng chọn giờ khác.");
            }
        }

        // 5. Kiểm tra sân đã được đặt chưa
        Optional<BookingDetail> existingBooking = bookingDetailRepository
                .findBySubCourtAndAvailableTimeAndDate(subCourt, time, bookingDate);

        if (existingBooking.isPresent()) {
            throw new IllegalArgumentException("Sân này đã được đặt cho khung giờ này vào ngày "
                    + bookingDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".");
        }

        // 6. Tạo booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setReceiverName(receiverName);
        booking.setReceiverAddress(receiverAddress);
        booking.setReceiverPhone(receiverPhone);
        booking.setAvailableTime(time);
        booking.setBookingDate(bookingDate);
        booking.setDepositPrice(product.getDepositPrice());
        booking.setStatus("Đã đặt");

        // 7. Tính toán giá
        double pricePerItem = product.getPrice() - (product.getPrice() * product.getSale() / 100);
        booking.setTotalPrice(pricePerItem);
        booking = bookingRepository.save(booking); // Lưu để có ID

        // 8. Tạo booking detail
        BookingDetail bookingDetail = new BookingDetail();
        bookingDetail.setBooking(booking);
        bookingDetail.setProduct(product);
        bookingDetail.setPrice(pricePerItem);
        bookingDetail.setSubCourt(subCourt);
        bookingDetail.setDate(bookingDate);
        bookingDetail.setSale(product.getSale());
        bookingDetail.setAvailableTime(time);
        bookingDetailRepository.save(bookingDetail);
    }
}
