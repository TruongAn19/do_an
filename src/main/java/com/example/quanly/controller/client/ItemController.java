package com.example.quanly.controller.client;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

// import static org.mockito.Mockito.times;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.quanly.domain.*;
import com.example.quanly.domain.dto.PaymentRequest;
import com.example.quanly.domain.dto.VnpayResponse;
import com.example.quanly.repository.*;
import com.example.quanly.service.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import com.example.quanly.domain.*;
import com.example.quanly.repository.*;
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

import com.example.quanly.domain.dto.ProductCriteriaDTO;
import com.example.quanly.service.ProductService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ItemController {

    ProductRepository productRepository;


    SubCourtRepository subCourtRepository;

    TimeRepository timeRepository;
    ProductService productService;
    RacketService racketService;
    UserService userService;
    BookingRepository bookingRepository;
    RentalToolService rentalToolService;
    RentalToolRepository rentalToolRepository;
    RacketRepository racketRepository;
    RestTemplateAutoConfiguration restTemplateAutoConfiguration;
    PaymentService paymentService;
    BookingDetailRepository bookingDetailRepository;


    @GetMapping("/main-products")
    public String getMainProductPage(Model model,
                                     ProductCriteriaDTO productCriteriaDTO,
                                     HttpServletRequest request,
                                     @RequestParam(value = "search", required = false) String searchTerm) {
        int page = 1;
        try {
            if (productCriteriaDTO.getPage().isPresent()) {
                // convert from String to int
                page = Integer.parseInt(productCriteriaDTO.getPage().get());
            } else {
                // page = 1
            }
        } catch (Exception e) {
            // page = 1
            // TODO: handle exception
        }

        // check sort price
        Pageable pageable = PageRequest.of(page - 1, 6);

        Optional<String> sortOpt = productCriteriaDTO.getSort();

        if (sortOpt != null && sortOpt.isPresent()) {
            String sort = sortOpt.get();
            if (sort.equals("gia-tang-dan")) {
                pageable = PageRequest.of(page - 1, 6, Sort.by(Product_.PRICE).ascending());
            } else if (sort.equals("gia-giam-dan")) {
                pageable = PageRequest.of(page - 1, 6, Sort.by(Product_.PRICE).descending());
            }
        }

        Page<Product> mainProduct;
        if (searchTerm != null && !searchTerm.isEmpty()) {
            // Chỉ tìm kiếm theo tên nếu có từ khóa
            mainProduct = productService.findByNameContaining(searchTerm, pageable);
        } else if (sortOpt != null && sortOpt.isPresent()
                && (productCriteriaDTO.getAddress() != null || productCriteriaDTO.getPrice() != null)) {
            mainProduct = this.productService.getAllProductWithSpec(pageable, productCriteriaDTO);
        } else {
            mainProduct = this.productService.getAllProduct(pageable);
        }

        int totalPages = Math.max(mainProduct.getTotalPages(), 0);

        String qs = request.getQueryString();
        if (qs != null && !qs.isBlank()) {
            qs = qs.replace("page=" + page, "");
        }

        model.addAttribute("listMainProducts", mainProduct.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("queryString", qs);
        return "client/product/main_product";
    }

    @GetMapping("/by-products")
    public String getByProductPage(Model model,
                                   ProductCriteriaDTO productCriteriaDTO,
                                   HttpServletRequest request) {
        // Lấy thông tin phân trang từ request
        int page = 0;
        int size = 10;
        try {
            page = Integer.parseInt(request.getParameter("page"));
            if (page < 0) page = 0;
        } catch (Exception e) {
            page = 0;
        }
        Pageable pageable = PageRequest.of(page, size);

        // Gọi service để lấy danh sách Racket có phân trang
        Page<Racket> byProduct = racketService.getAllRacket(pageable);

        // Đưa dữ liệu ra model
        model.addAttribute("listByProduct", byProduct.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", byProduct.getTotalPages());

        // QueryString để giữ nguyên filter/search nếu có
        String qs = request.getQueryString();
        model.addAttribute("queryString", qs != null ? qs : "");

        return "client/racket/by_product";
    }


    @GetMapping("/mainProduct/{productId}")
    public String getMainProducts(Model model, @PathVariable long productId) {
        Product product = this.productService.getProductByID(productId);
        List<AvailableTime> availableTime = this.productService.getAllTime();
        double discountPrice = product.getPrice() - (product.getPrice() * product.getSale() / 100);
        model.addAttribute("discountPrice", discountPrice);
        model.addAttribute("availableTime", availableTime); // Gán vào model
        model.addAttribute("product", product);
        model.addAttribute("id", productId);
        return "client/product/detailMainProduct";
    }



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
            productService.handlePlaceBooking(currentUser, session,
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
