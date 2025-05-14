package com.example.quanly.controller.admin;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.example.quanly.domain.Product;
import com.example.quanly.domain.RentalTool;
import com.example.quanly.repository.RentalToolRepository;
import com.example.quanly.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    RentalToolRepository rentalToolRepository;

    @GetMapping("/admin/booking")
    public String getBooking(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(value = "search", required = false) String searchTerm,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            Model model) {

        Page<Booking> bookingPage;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());

        if (date != null) {
            bookingPage = bookingService.fetchBookingsByDate(date, pageable);
            model.addAttribute("selectedDate", date);
        } else if (searchTerm != null && !searchTerm.isEmpty()) {
            bookingPage = bookingService.fetchBookingCode(searchTerm, pageable);
            model.addAttribute("searchTerm", searchTerm);
        } else {
            bookingPage = bookingService.fetchAllBookings(pageable);
        }

        model.addAttribute("bookings", bookingPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookingPage.getTotalPages());

        return "admin/booking/show";
    }


    @GetMapping("/admin/booking/{id}")
    public String getBookingDetailPage(Model model, @PathVariable long id) {
        Booking booking = this.bookingService.fetchBookingById(id).get();
        List<RentalTool> rentalTool = this.rentalToolRepository.findRentalToolsByBookingId(String.valueOf(booking.getId()));
        model.addAttribute("booking", booking);
        model.addAttribute("id", id);
        model.addAttribute("bookingDetails", booking.getBookingDetails());
        model.addAttribute("rentalTool", rentalTool);
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
