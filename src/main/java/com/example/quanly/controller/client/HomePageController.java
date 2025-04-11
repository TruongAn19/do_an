package com.example.quanly.controller.client;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.quanly.domain.Order;
import com.example.quanly.domain.Product;
import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.RegisterDTO;
import com.example.quanly.service.OrderService;
import com.example.quanly.service.ProductService;
import com.example.quanly.service.UploadService;
import com.example.quanly.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class HomePageController {

    private final ProductService productService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final OrderService orderService;
    private final UploadService uploadService;
    
        public HomePageController(
                ProductService productService,
                UserService userService,
                PasswordEncoder passwordEncoder,
                OrderService orderService,
                UploadService uploadService) {
            this.productService = productService;
            this.userService = userService;
            this.passwordEncoder = passwordEncoder;
            this.orderService = orderService;
            this.uploadService = uploadService;
    }

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
        Page<Product> mainProducts = this.productService.getAllMainProduct(pageable);
        Page<Product> byProducts = this.productService.getAllByProduct(pageable);
        List<Product> listMainProducts = mainProducts.getContent();
        List<Product> listByProducts = byProducts.getContent();
        model.addAttribute("mainProducts", listMainProducts);
        model.addAttribute("byProducts", listByProducts);
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

    @GetMapping("access-denied")
    public String getDenyPage(Model model) {
        return "client/auth/access_deny";
    }

    @GetMapping("/order-history")
    public String getOrderHistoryPage(Model model, HttpServletRequest request) {
        User currentUser = new User();// null
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        List<Order> orders = this.orderService.fetchOrderByUser(currentUser);
        model.addAttribute("orders", orders);
        return "client/cart/history_order";
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
}
