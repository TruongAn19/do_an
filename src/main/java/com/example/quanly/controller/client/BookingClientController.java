package com.example.quanly.controller.client;

import com.example.quanly.domain.*;
import com.example.quanly.domain.dto.AvailableTimeDTO;
import com.example.quanly.domain.dto.PaymentRequest;
import com.example.quanly.domain.dto.VnpayResponse;
import com.example.quanly.repository.BookingDetailRepository;
import com.example.quanly.repository.ProductRepository;
import com.example.quanly.repository.SubCourtRepository;
import com.example.quanly.repository.TimeRepository;
import com.example.quanly.service.BookingService;
import com.example.quanly.service.PaymentService;
import com.example.quanly.service.ProductService;
import com.example.quanly.service.RacketService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BookingClientController {
    ProductService productService;
    RacketService racketService;
    SubCourtRepository subCourtRepository;
    TimeRepository timeRepository;
    BookingDetailRepository bookingDetailRepository;
    BookingService bookingService;
    PaymentService paymentService;
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
    public List<AvailableTimeDTO> getAvailableTimes(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("courtId") Long courtId) {

        SubCourt court = subCourtRepository.getById(courtId);

        // Lấy tất cả bookingDetail đã được đặt cho sân và ngày này
        List<BookingDetail> bookings = bookingDetailRepository.findBySubCourtAndDate(court, date);

        // Tập ID giờ đã bị đặt
        Set<Long> bookedTimeIds = bookings.stream()
                .map(b -> b.getAvailableTime().getId())
                .collect(Collectors.toSet());

        // Lấy tất cả khung giờ
        List<AvailableTime> allTimes = timeRepository.findAll();

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        log.info("Date: {}, CourtId: {}", date, courtId);
        log.info("Booked time IDs: {}", bookedTimeIds);
        log.info("All times: {}", allTimes.stream().map(AvailableTime::getId).collect(Collectors.toList()));

        List<AvailableTime> availableTimes = allTimes.stream()
                .filter(time -> {
                    boolean isAvailable = true;
                    // Nếu là ngày hiện tại, loại bỏ giờ đã qua
                    if (date.equals(today) && time.getTime().isBefore(now)) {
                        isAvailable = false;
                    }
                    // Loại bỏ giờ đã bị đặt
                    if (bookedTimeIds.contains(time.getId())) {
                        isAvailable = false;
                    }
                    log.info("Time ID: {}, Time: {}, Available? {}", time.getId(), time.getTime(), isAvailable);
                    return isAvailable;
                })
                .collect(Collectors.toList());
        return availableTimes.stream()
                .map(AvailableTimeDTO::new)
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
            Booking booking = bookingService.handlePlaceBooking(currentUser, session,
                    receiverName, receiverAddress, receiverPhone,
                    productId, timeId, subCourtId, bookingDate);

            redirectAttributes.addFlashAttribute("successMessage", "Đặt sân thành công!");
            return "redirect:/submit-booking/"+ booking.getId();

        } catch (IllegalArgumentException e) {
            // Tính toán lại tổng tiền
            double price = product.getPrice();
            double discount = product.getSale() / 100.0;
            double totalPrice = price  - (price * discount);

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

    @GetMapping("/thanks/{bookingCode}/{courtId}/court")
    public String bookingSuccess(Model model, @PathVariable String bookingCode, @PathVariable long courtId) {
        // Lấy thông tin booking từ mã bookingCode
        List<Racket> racketList = racketService.getAvailableRacketsByCourt(courtId);
        model.addAttribute("racketList", racketList);
        model.addAttribute("bookingCode", bookingCode);
        return "client/booking/thanks";
    }

    @GetMapping("/submit-booking/{bookingId}")
    public String paymentBooking(@ModelAttribute RentalTool rentalTool,
                                       @PathVariable Long bookingId,
                                       HttpServletRequest request
                                       ) {
            Booking booking = bookingService.fetchBookingById(bookingId).orElse(null);
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setId(bookingId);
            paymentRequest.setAmount(booking.getDepositPrice());
            paymentRequest.setType("BOOKING");
            paymentRequest.setRedirectUrl("");
            VnpayResponse vnpayResponse = paymentService.createVnPayPayment(paymentRequest, request);
            return "redirect:" + vnpayResponse.getPaymentUrl();
    }

    @GetMapping("/payment/success/{bookingId}/booking")
    public String paymentSuccess(Model model, @PathVariable Long bookingId) {
        // Giả sử sau khi thanh toán thành công, hệ thống nhận kết quả và gửi thông báo thành công.
        String message = "Thanh toán thành công! Cảm ơn bạn đã sử dụng dịch vụ.";
        // Thêm thông báo vào model
        model.addAttribute("message", message);
        model.addAttribute("bookingId", bookingId);
        // Trả về trang JSP hiển thị thông báo thành công
        return "client/payment/booking-payment-success";
    }

    @GetMapping("/rental/start/{bookingId}/booking")
    public String redirectRentalRacket(@PathVariable Long bookingId)
    {
        Booking booking = bookingService.fetchBookingById(bookingId).orElse(null);
        Long courtId = booking.getBookingDetails().get(0).getProduct().getId();
        return "redirect:/thanks/" + booking.getBookingCode() + "/" + courtId + "/court";
    }
}
