package com.example.quanly.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.quanly.domain.Booking;
import com.example.quanly.domain.BookingDetail;
import com.example.quanly.domain.User;
import com.example.quanly.repository.BookingDetailRepository;
import com.example.quanly.repository.BookingRepository;


@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;

    public BookingService(
            BookingRepository bookingRepository,
            BookingDetailRepository bookingDetailRepository) {
        this.bookingDetailRepository = bookingDetailRepository;
        this.bookingRepository = bookingRepository;
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
        if (bOptional.isPresent()) {
            Booking currentBooking = bOptional.get();
            currentBooking.setStatus(booking.getStatus());
            this.bookingRepository.save(currentBooking);
        }
    }

    public List<Booking> fetchBookingByUser(User user) {
        return this.bookingRepository.findByUser(user);
    }

    public List<Booking> fetchBookingsByDate(LocalDate date) {
    return bookingRepository.findByBookingDetailsDate(date);
}
}
