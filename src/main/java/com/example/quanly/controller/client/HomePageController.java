package com.example.quanly.controller.client;

import com.example.quanly.domain.*;
import com.example.quanly.domain.dto.RegisterDTO;
import com.example.quanly.repository.BookingDetailRepository;
import com.example.quanly.repository.RentalToolRepository;
import com.example.quanly.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.YearMonth;
import java.util.*;


@Slf4j
@Controller
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class HomePageController {

    ProductService productService;
    UserService userService;
    PasswordEncoder passwordEncoder;
    UploadService uploadService;
    BookingService bookingService;
    RentalToolRepository rentalToolRepository;
    RentalToolService rentalToolService;
    RacketService racketService;
    BookingDetailRepository bookingDetailRepository;

    @GetMapping("/rules")
    public String getRulus() {
        return ("client/homepage/rules");
    }

    @GetMapping("/introduce")
    public String getIntroduce() {
        return ("client/homepage/introduce");
    }

    @GetMapping("/HomePage")
    public String getHomePage(Model model, @RequestParam("page") Optional<String> pageOptional) {
        int page = pageOptional.map(Integer::parseInt).orElse(1);
        Pageable pageable = PageRequest.of(page - 1, 4);

        // Paging sản phẩm và vợt
        Page<Product> mainProducts = productService.getAllProductClient(pageable);
        Page<Racket> byProducts = racketService.getAllRacket(pageable);

        // Lấy tháng hiện tại và tháng trước
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        // Top 4 sân tháng này, nếu không có thì lấy tháng trước
        List<Long> topProductIds = bookingDetailRepository.findTop4ProductIdsByMonth(
                currentMonth.getYear(), currentMonth.getMonthValue(), PageRequest.of(0, 4));
        if (topProductIds.isEmpty()) {
            topProductIds = bookingDetailRepository.findTop4ProductIdsByMonth(
                    previousMonth.getYear(), previousMonth.getMonthValue(), PageRequest.of(0, 4));
        }

        List<Product> topProducts = topProductIds.stream()
                .map(productService::fetchProductById)
                .filter(opt -> opt.isPresent() && !"DELETED".equals(opt.get().getStatus()))
                .map(Optional::get)
                .toList();

        // Top 4 vợt tháng này, nếu không có thì lấy tháng trước
        List<Long> topRacketIds = rentalToolRepository.findTop4RacketIdsByMonth(
                currentMonth.getYear(), currentMonth.getMonthValue(), PageRequest.of(0, 4));
        if (topRacketIds.isEmpty()) {
            topRacketIds = rentalToolRepository.findTop4RacketIdsByMonth(
                    previousMonth.getYear(), previousMonth.getMonthValue(), PageRequest.of(0, 4));
        }

        List<Racket> topRackets = topRacketIds.stream()
                .map(racketService::getRacketById)
                .filter(opt -> opt.isPresent() && !"DELETED".equals(opt.get().getStatus()))
                .map(Optional::get)
                .toList();

        // Add to model
        model.addAttribute("mainProducts", mainProducts.getContent());
        model.addAttribute("racketList", byProducts.getContent());
        model.addAttribute("topProducts", topProducts);
        model.addAttribute("topRackets", topRackets);

        return "client/homepage/show";
    }




    @GetMapping("/register")
    public String getMethodName(Model model) {
        model.addAttribute("registerUser", new RegisterDTO());
        return "client/auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(Model model, @ModelAttribute("registerUser") @Valid RegisterDTO registerDTO,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "client/auth/register";
        }
        User user = this.userService.registerDTOtoUser(registerDTO);
        String hashPassword = this.passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPassword);
        user.setRole(this.userService.getRoleByName("USER"));
        this.userService.handleSaveUser(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String getLoginPage(Model model) {
        return "client/auth/login";
    }

    @GetMapping("/access-denied")
    public String goHomePage(Authentication authentication) {
        if (authentication != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                if (role.equals("ROLE_STAFF")) {
                    return "redirect:/admin/booking";
                } else if (role.equals("ROLE_USER")) {
                    return "redirect:/HomePage";
                }
            }
        }
        return "redirect:/accessDenied";
    }

    @GetMapping("/booking-history")
    public String getBookingHistoryPage(
            Model model,
            HttpServletRequest request,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {

        HttpSession session = request.getSession(false);
        long userId = (long) session.getAttribute("id");

        Pageable pageable = PageRequest.of(page, size, Sort.by("bookingDate").descending());
        Page<Booking> bookingsPage = bookingService.fetchBookingByUserWithPaging(userId, pageable);

        model.addAttribute("bookings", bookingsPage.getContent());
        model.addAttribute("currentPage", bookingsPage.getNumber());
        model.addAttribute("totalPages", bookingsPage.getTotalPages());

        return "client/booking/history_booking";
    }

    @GetMapping("/booking-history/{id}")
    public String getBookingDetail(@PathVariable("id") Long id, Model model) {
        Booking booking = this.bookingService.fetchBookingById(id).get();
        List<RentalTool> rentalTool = this.rentalToolRepository.findRentalToolsByBookingId(String.valueOf(booking.getId()));
        model.addAttribute("booking", booking);
        model.addAttribute("id", id);
        model.addAttribute("bookingDetails", booking.getBookingDetails());
        model.addAttribute("rentalTool", rentalTool);
        return "client/booking/history_booking_detail";
    }



    @GetMapping("/rental-history")
    public String getRentalHistoryPage(Model model, HttpServletRequest request) {
        User currentUser = new User();// null
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        List<RentalTool> rentalTools = this.rentalToolService.fetchRentalByUser(currentUser);
        Collections.reverse(rentalTools);
        model.addAttribute("rentalHistories", rentalTools);
        return "client/racket/rental_history";
    }

    @GetMapping("/profile")
    public String getProfilePage(Model model, HttpServletRequest request) {
        User currentUser = new User();
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        User user = this.userService.getUserById(id);
        model.addAttribute("user", user);
        System.out.println(user);
        return "client/homepage/profile";
    }

    @GetMapping("/update_profile/{userId}")
    public String getUpdateProfilePage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("id");
        User currentUser = this.userService.getUserById(userId);
        model.addAttribute("updateUser", currentUser);
        return "client/homepage/update_profile";
    }

    @PostMapping("/update_profile")
    public String handleUpdateProfile(Model model,
            @ModelAttribute("updateUser") User user,
            @RequestParam(value = "avatarFile", required = false) MultipartFile file,
            HttpServletRequest request) {
        User currentUser = new User();// null
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        User updateUser = this.userService.updateToUser(id);

        if (file != null && !file.isEmpty()) {
            String avatar = this.uploadService.handleSaveUploadFile(file, "avatar");
            updateUser.setAvatar(avatar);
        }
        updateUser.setFullName(user.getFullName());
        updateUser.setEmail(user.getEmail());
        updateUser.setAddress(user.getAddress());
        updateUser.setPhone(user.getPhone());

        this.userService.handleSaveUser(updateUser);
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    @ResponseBody
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Principal principal,
            HttpServletRequest request,
            HttpServletResponse response  // thêm đúng response để không bị xung đột
    ) {
        Map<String, String> result = new HashMap<>();
        User currentUser = userService.findByEmail(principal.getName());

        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            result.put("error", "Mật khẩu cũ không đúng");
            return ResponseEntity.badRequest().body(result);
        }

        if (!newPassword.equals(confirmPassword)) {
            result.put("error", "Xác nhận mật khẩu không khớp");
            return ResponseEntity.badRequest().body(result);
        }

        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userService.handleSaveUser(currentUser);
        result.put("success", "Mật khẩu đã được thay đổi thành công");

        // Đăng xuất người dùng
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        result.put("success", "Đổi mật khẩu thành công. Đang đăng xuất...");
        return ResponseEntity.ok(result);
    }



}
