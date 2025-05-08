package com.example.quanly.controller.client;

import com.example.quanly.config.VnpayConfig;
import com.example.quanly.config.VnpayUtil;
import com.example.quanly.domain.*;
import com.example.quanly.domain.dto.PaymentRequest;
import com.example.quanly.domain.dto.VnpayResponse;
import com.example.quanly.repository.BookingRepository;
import com.example.quanly.repository.RacketRepository;
import com.example.quanly.repository.RentalToolRepository;
import com.example.quanly.service.PaymentService;
import com.example.quanly.service.RacketService;
import com.example.quanly.service.RentalToolService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class RentalController {

    private final RacketService racketService;
    private final RacketRepository racketRepository;
    private final BookingRepository bookingRepository;
    private final RentalToolRepository rentalToolRepository;
    private final PaymentService paymentService;
    private final RentalToolService rentalToolService;
    private final VnpayConfig vnpayConfig;

    public RentalController(RacketService racketService, RacketRepository racketRepository, BookingRepository bookingRepository, RentalToolRepository rentalToolRepository, PaymentService paymentService, RentalToolService rentalToolService, @Qualifier("vnpayConfig") VnpayConfig vnpayConfig) {
        this.racketService = racketService;
        this.racketRepository = racketRepository;
        this.bookingRepository = bookingRepository;
        this.rentalToolRepository = rentalToolRepository;
        this.paymentService = paymentService;
        this.rentalToolService = rentalToolService;
        this.vnpayConfig = vnpayConfig;
    }

    @GetMapping("/user/rental-page/{racketId}")
    public String goToRentalPage(@PathVariable("racketId") Long racketId,
                                 @RequestParam(value = "type") String type,
                                 @RequestParam(value = "bookingCode", required = false) String bookingCode,
                                 Model model) {

        model.addAttribute("racket", racketService.getRacketById(racketId).orElse(null));
        model.addAttribute("typeOrder", type);
        if(bookingCode!=null)
            model.addAttribute("bookingCode", bookingCode);

        return "client/racket/rental_page";
    }



    @PostMapping("/submit-rental")
    public String submitRental(@ModelAttribute RentalTool rentalTool, Model model,
                               @RequestParam(value = "bookingId", required = false) String bookingId,
                               HttpServletRequest request) {


        // Kiểm tra nếu loại thuê là 'ON_SITE', thêm bookingCode vào model
        if ("ON_SITE".equals(rentalTool.getType())) {
            Booking booking = (bookingRepository.findByBookingCode(bookingId));
            rentalTool.setBookingId(booking.getBookingCode());
        }
        User currentUser = new User();// null
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        rentalToolService.handleSubmitRental(rentalTool, model, currentUser);
        // Chuyển hướng theo type
        return rentalTool.getType().equals("DAILY")
                ? "client/racket/rental_checkout"
                : "client/racket/rental_success";
    }


    @PostMapping("/submit-checkout-rental")
    public String submitRentalCheckout(@ModelAttribute RentalTool rentalTool,
                                       @RequestParam("paymentMethod") String paymentMethod,
                                       @RequestParam("racketName") String racketName,
                                       @RequestParam("rentalToolId") Long rentalToolId,
                                       HttpServletRequest request,
                                       Model model) {

        if (paymentMethod.equals("VNPAY")) {
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setId(rentalToolId);
            paymentRequest.setAmount(rentalTool.getPrice());
            paymentRequest.setType("RENTAL_TOOL");
            paymentRequest.setRedirectUrl("");
            VnpayResponse vnpayResponse = paymentService.createVnPayPayment(paymentRequest, request);
            return "redirect:" + vnpayResponse.getPaymentUrl();
        } else {
            model.addAttribute("rentalTool", rentalTool);
            model.addAttribute("racketName", racketName);
            return "client/racket/rental_success";
        }
    }

    @GetMapping("/payment/success")
    public String paymentSuccess(Model model) {
        // Giả sử sau khi thanh toán thành công, hệ thống nhận kết quả và gửi thông báo thành công.
        String message = "Thanh toán thành công! Cảm ơn bạn đã sử dụng dịch vụ.";
        // Thêm thông báo vào model
        model.addAttribute("message", message);
        // Trả về trang JSP hiển thị thông báo thành công
        return "client/payment/payment-success";
    }

    @GetMapping("/payment/error")
    public String paymentError(Model model) {
        // Giả sử sau khi thanh toán thất bại, hệ thống nhận kết quả và gửi thông báo thất bại.
        String message = "Thanh toán thất bại! Vui lòng thử lại.";
        // Thêm thông báo vào model
        model.addAttribute("message", message);
        // Trả về trang JSP hiển thị thông báo thất bại
        return "client/payment/payment-error";
    }




    @GetMapping("/payment/vnpay-callback")
    public String handleVnpayCallback(HttpServletRequest request) {
        // Lấy các tham số từ VNPay gửi lại
        String status = request.getParameter("vnp_ResponseCode");
        String type = request.getParameter("vnp_OrderInfo").split("-")[1];
        if(type.equals("RENTAL_TOOL")){
            String rentalToolId = request.getParameter("vnp_OrderInfo").split("-")[0];
            if (status == null || rentalToolId == null) {
                throw new RuntimeException("Invalid callback parameters");
            }
            if ("00".equals(status)) {
                RentalTool rentalTool = rentalToolRepository.findById(Long.parseLong(rentalToolId)).orElseThrow(() -> new RuntimeException("RentalTool not found"));
                rentalTool.setStatus(RentalToolStatus.PAID);
                rentalToolRepository.save(rentalTool);
                return "redirect:/payment/success"; // Trang thành công
            } else {
                return "redirect:/payment/error"; // Trang thất bại
            }
        }else
        { String bookingId = request.getParameter("vnp_OrderInfo").split("-")[0];
            Booking booking = bookingRepository.findById(Long.parseLong(bookingId)).orElseThrow(() -> new RuntimeException("RentalTool not found"));

            if ("00".equals(status)) {
                booking.setStatus("Đã đặt");
                bookingRepository.save(booking);
                return "redirect:/payment/success/"+ bookingId + "/booking"; // Trang thành công
            } else {
                bookingRepository.delete(booking);
                return "redirect:/payment/error"; // Trang thất bại
            }
        }


    }
}
