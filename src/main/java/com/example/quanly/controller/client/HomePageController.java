package com.example.quanly.controller.client;

import java.security.Principal;
import java.util.*;

import com.example.quanly.domain.*;
import com.example.quanly.repository.RentalToolRepository;
import com.example.quanly.service.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.*;

import com.example.quanly.domain.dto.RegisterDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;


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



    @GetMapping("/HomePage")
    public String getHomePage(Model model, @RequestParam("page") Optional<String> pageOptional) {
        int page = 1;
        try {
            if (pageOptional.isPresent()) {
                // convert from String to int
                page = Integer.parseInt(pageOptional.get());
            } else {
                // page = 1
            }
        } catch (Exception e) {
            // page = 1
            // TODO: handle exception
        }
        Pageable pageable = PageRequest.of(page - 1, 4);
        Page<Product> mainProducts = this.productService.getAllProduct(pageable);
        Page<Racket> byProducts = this.racketService.getAllRacket(pageable);
        List<Product> listMainProducts = mainProducts.getContent();
        List<Racket> racketList = byProducts.getContent();
        model.addAttribute("mainProducts", listMainProducts);
        model.addAttribute("racketList", racketList);
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
    public String getBookingHistoryPage(Model model, HttpServletRequest request) {
        User currentUser = new User();// null
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        List<Booking> bookings = this.bookingService.fetchBookingByUser(currentUser);
        bookings.sort(Comparator.comparing(
                Booking::getBookingDate,
                Comparator.nullsLast(Comparator.naturalOrder())
        ).reversed());
        model.addAttribute("bookings", bookings);
        return "client/booking/history_booking";
    }

    @GetMapping("/rental-history")
    public String getRentalHistoryPage(Model model, HttpServletRequest request) {
        User currentUser = new User();// null
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        List<RentalTool> rentalTools = this.rentalToolService.fetchRentalByUser(currentUser);
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
        String avatar = this.uploadService.handleSaveUploadFile(file, "avatar");
        updateUser.setFullName(user.getFullName());
        updateUser.setEmail(user.getEmail());
        updateUser.setAddress(user.getAddress());
        updateUser.setPhone(user.getPhone());
        updateUser.setAvatar(avatar);
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
