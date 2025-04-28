package com.example.quanly.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.example.quanly.domain.*;
import com.example.quanly.repository.RentalToolRepository;
import org.springframework.stereotype.Service;

import com.example.quanly.repository.BookingDetailRepository;
import com.example.quanly.repository.BookingRepository;


@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final RentalToolRepository rentalToolRepository;

    public BookingService(
            BookingRepository bookingRepository,
            BookingDetailRepository bookingDetailRepository, RentalToolRepository rentalToolRepository) {
        this.bookingDetailRepository = bookingDetailRepository;
        this.bookingRepository = bookingRepository;
        this.rentalToolRepository = rentalToolRepository;
    }

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
            if(currentBooking.getStatus().equals("Đã thanh toán")) {
                List<RentalTool> rentalTools = rentalToolRepository.findRentalToolsByBookingId(currentBooking.getId()+"");
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
}
