package com.example.quanly.controller.admin;

import com.example.quanly.domain.User;
import com.example.quanly.service.UploadService;
import com.example.quanly.service.UserService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class UserController {

    private final UserService userService;
    private final UploadService uploadService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, UploadService uploadService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.uploadService = uploadService;
        this.passwordEncoder = passwordEncoder;
    }

    @RequestMapping("/admin/user")
    public String getUserPage(Model model) {
        List<User> users = this.userService.getAllUser();
        model.addAttribute("users", users);
        return "admin/user/show";
    }

    @GetMapping("/admin/user/create") // GET
    public String getCreateUserPage(Model model) {
        model.addAttribute("newUser", new User());
        return "admin/user/create";
    }

    @PostMapping(value = "/admin/user/create")
    public String createUserPage(Model model, @ModelAttribute("newUser") @Valid User user,
            BindingResult bindingResult,
            @RequestParam("avatarFile") MultipartFile file) {

        List<FieldError> errors = bindingResult.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println(">>>>" + error.getField() + " - " + error.getDefaultMessage());
        }
        if (bindingResult.hasErrors()) {
            return "admin/user/create";
        }
        String avatar = this.uploadService.handleSaveUploadFile(file, "avatar");
        String hashPassword = this.passwordEncoder.encode(user.getPassword());
        user.setAvatar(avatar);
        user.setPassword(hashPassword);
        user.setRole(this.userService.getRoleByName(user.getRole().getName()));
        this.userService.handleSaveUser(user);
        return "redirect:/admin/user";
    }

    @RequestMapping("/admin/user/{userId}")
    public String getUserDetail(Model model, @PathVariable long userId) {
        User user = this.userService.getUserById(userId);
        model.addAttribute("user", user);
        model.addAttribute("id", userId);
        return "admin/user/user_detail";
    }

    @RequestMapping("/admin/user/update_user/{userId}") // GET
    public String getUpdateUserPage(Model model, @PathVariable long userId) {
        User currentUser = this.userService.getUserById(userId);
        model.addAttribute("newUser", currentUser);
        return "admin/user/update_user";
    }

    @PostMapping("/admin/user/update_user")
    public String postUpdateUser(Model model, @ModelAttribute("newUser") User user) {
        User currentUser = this.userService.getUserById(user.getId());
        if (currentUser != null) {
            currentUser.setAddress(user.getAddress());
            currentUser.setFullName(user.getFullName());
            currentUser.setPhone(user.getPhone());

            // bug here
            this.userService.handleSaveUser(currentUser);
        }
        return "redirect:/admin/user";
    }

    @GetMapping("/admin/user/delete_user/{userId}")
    public String getDeleteUserPage(Model model, @PathVariable long userId) {
        model.addAttribute("id", userId);
        model.addAttribute("user", new User());
        return "admin/user/delete_user";
    }

    @PostMapping("/admin/user/delete_user")
    public String postDeleteUser(Model model, @ModelAttribute("user") User user) {
        this.userService.deleteAUser(user.getId());
        return "redirect:/admin/user";
    }
}
