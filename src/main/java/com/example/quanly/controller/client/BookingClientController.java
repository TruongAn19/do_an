package com.example.quanly.controller.client;

import com.example.quanly.domain.*;
import com.example.quanly.repository.BookingDetailRepository;
import com.example.quanly.repository.ProductRepository;
import com.example.quanly.repository.SubCourtRepository;
import com.example.quanly.repository.TimeRepository;
import com.example.quanly.service.BookingService;
import com.example.quanly.service.ProductService;
import com.example.quanly.service.RacketService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BookingClientController {
    ProductService productService;
    RacketService racketService;
    SubCourtRepository subCourtRepository;
    TimeRepository timeRepository;
    BookingDetailRepository bookingDetailRepository;
    BookingService bookingService;
    ProductRepository productRepository;

    @GetMapping("/booking/{productId}")
    public String getBookingPage(Model model,
                                 HttpServletRequest request,
                                 @PathVariable long productId) {
        User currentUser = new User();// null
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        Product product = this.productService.getProductByID(productId);
        List<AvailableTime> allTimes = this.productService.getAllTime();
        double totalPrice = 0;

        double price = product.getPrice();
        long quantity = 1;
        double discount = product.getSale() / 100.0;
        // totalPrice = (price * quantity) - (price * quantity * discount);

        totalPrice += (price * quantity) - (price * quantity * discount);

        List<SubCourt> courts = this.productService.getAllCourtsByProduct(product);
        model.addAttribute("courts", courts);

        model.addAttribute("product", product);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("availableTime", allTimes);
        return "client/booking/booking_page";
    }

    @GetMapping("/api/available-time")
    @ResponseBody
    public List<AvailableTime> getAvailableTimes(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("courtId") Long courtId) {
        List<AvailableTime> allTimes = timeRepository.findAll();
        SubCourt court = subCourtRepository.getById(courtId);
        List<BookingDetail> bookings = bookingDetailRepository.findBySubCourtAndDate(court, date);
        Set<Long> bookedTimeIds = bookings.stream()
                .map(o -> o.getAvailableTime().getId())
                .collect(Collectors.toSet());
        return allTimes.stream()
                .filter(time -> {
                    if (date.equals(LocalDate.now()) && time.getTime().isBefore(LocalTime.now())) {
                        return false;
                    }
                    return !bookedTimeIds.contains(time.getId());
                })
                .collect(Collectors.toList());

    }

    @PostMapping("/place-booking")
    public String handlePlaceBooking(Model model,
                                     HttpServletRequest request,
                                     @RequestParam("receiverName") String receiverName,
                                     @RequestParam("receiverAddress") String receiverAddress,
                                     @RequestParam("receiverPhone") String receiverPhone,
                                     @RequestParam("productId") long productId,
                                     @RequestParam("availableTimeId") long timeId,
                                     @RequestParam("courtId") long subCourtId,
                                     @RequestParam("bookingDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookingDate,
                                     RedirectAttributes redirectAttributes) {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("id") == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để đặt sân");
            return "redirect:/login";
        }

        long userId = (long) session.getAttribute("id");
        User currentUser = new User();
        currentUser.setId(userId);

        // Lấy thông tin sản phẩm và các dữ liệu cần thiết
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));
        List<SubCourt> subCourts = this.productService.getAllCourtsByProduct(product);
        List<AvailableTime> allTimes = timeRepository.findAll();

        try {
            bookingService.handlePlaceBooking(currentUser, session,
                    receiverName, receiverAddress, receiverPhone,
                    productId, timeId, subCourtId, bookingDate);

            redirectAttributes.addFlashAttribute("successMessage", "Đặt sân thành công!");
            return "redirect:/thanks";

        } catch (IllegalArgumentException e) {
            // Tính toán lại tổng tiền
            double price = product.getPrice();
            double discount = product.getSale() / 100.0;
            double totalPrice = price  - (price * discount);

            List<Racket> rackets = this.racketService.getAvailableRacketsByCourt(productId);

            // Thiết lập lại các thông tin cần hiển thị
            model.addAttribute("rackets", rackets);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("product", product);
            model.addAttribute("courts", subCourts);
            model.addAttribute("availableTime", allTimes);
            model.addAttribute("totalPrice", totalPrice);

            // Giữ lại các giá trị đã nhập
            model.addAttribute("receiverName", receiverName);
            model.addAttribute("receiverAddress", receiverAddress);
            model.addAttribute("receiverPhone", receiverPhone);
            model.addAttribute("selectedCourtId", subCourtId);
            model.addAttribute("selectedTimeId", timeId);
            model.addAttribute("selectedBookingDate", bookingDate);

            return "client/booking/booking_page";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi đặt sân: " + e.getMessage());
            return "redirect:/booking_page";
        }
    }

    @GetMapping("/thanks")
    public String bookingSuccess() {
        return "client/booking/thanks";
    }
}
