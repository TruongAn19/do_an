package com.example.quanly.controller.admin;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.example.quanly.domain.Product;
import com.example.quanly.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.quanly.domain.Booking;
import com.example.quanly.service.BookingService;

@Controller
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BookingController {
    BookingService bookingService;

    @GetMapping("/admin/booking")
    public String getBooking(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            Model model,
            @RequestParam(value = "search", required = false) String searchTerm) {

        List<Booking> bookings;
        if (date != null) {
            bookings = this.bookingService.fetchBookingsByDate(date);
        }else if(searchTerm != null && !searchTerm.isEmpty()){
            bookings = this.bookingService.fetchBookingCode(searchTerm);
            model.addAttribute("searchTerm", searchTerm);
        } else {
            bookings = this.bookingService.fetchAllBookings();
        }

        model.addAttribute("bookings", bookings);
        model.addAttribute("selectedDate", date);
        return "admin/booking/show";
    }

    @GetMapping("/admin/booking/{id}")
    public String getBookingDetailPage(Model model, @PathVariable long id) {
        Booking booking = this.bookingService.fetchBookingById(id).get();
        model.addAttribute("booking", booking);
        model.addAttribute("id", id);
        model.addAttribute("bookingDetails", booking.getBookingDetails());
        return "admin/booking/detail";
    }

    @GetMapping("/admin/booking/delete/{id}")
    public String getDeleteBookingPage(Model model, @PathVariable long id) {
        model.addAttribute("id", id);
        model.addAttribute("newBooking", new Booking());
        return "admin/booking/delete";
    }

    @PostMapping("/admin/booking/delete")
    public String postDeleteBooking(@ModelAttribute("newBooking") Booking booking) {
        this.bookingService.deleteBookingById(booking.getId());
        return "redirect:/admin/booking";
    }

    @GetMapping("/admin/booking/update/{id}")
    public String getUpdateBookingPage(Model model, @PathVariable long id) {
        Optional<Booking> currentBooking = this.bookingService.fetchBookingById(id);
        model.addAttribute("newBooking", currentBooking.get());
        return "admin/booking/update";
    }

    @PostMapping("/admin/booking/update")
    public String handleUpdateBooking(@ModelAttribute("newBooking") Booking booking) {
        this.bookingService.updateBooking(booking);
        return "redirect:/admin/booking";
    }
}
