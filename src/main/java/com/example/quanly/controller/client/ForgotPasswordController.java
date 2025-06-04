package com.example.quanly.controller.client;

import com.example.quanly.domain.PasswordResetToken;
import com.example.quanly.domain.User;
import com.example.quanly.domain.utility.PasswordResetTokenDAO;
import com.example.quanly.domain.utility.UserDAO;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("/api")
public class ForgotPasswordController {

    UserDAO userDAO;
    PasswordResetTokenDAO tokenDAO;
    JavaMailSender mailSender;
    PasswordEncoder passwordEncoder;


    @GetMapping("/forgot-password")
    public String showForgotForm() {
        return "client/auth/forgot-password";
    }

    @Transactional
    @PostMapping("/forgot-password")
    public String handleForgot(@RequestParam("email") String email, Model model) {
        User user = userDAO.findByEmail(email);
        if (user == null) {
            model.addAttribute("message", "Email không tồn tại!");
            return "client/auth/forgot-password";
        }

        // 🔥 Xoá token cũ nếu tồn tại
        PasswordResetToken existingToken = tokenDAO.findByUser(user);
        if (existingToken != null) {
            tokenDAO.delete(existingToken);
        }

        // ✅ Tạo token mới
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        tokenDAO.save(resetToken);

        // Gửi mail
        String resetLink = "http://localhost:8080/api/reset-password?token=" + token;
        sendEmail(user.getEmail(), resetLink);

        model.addAttribute("message", "Liên kết đặt lại mật khẩu đã được gửi đến email.");
        return "client/auth/forgot-password";
    }


    private void sendEmail(String to, String link) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject("Đặt lại mật khẩu");
        mail.setText("Nhấn vào link để đặt lại mật khẩu: \n" + link);
        mailSender.send(mail);
    }

    @GetMapping("/reset-password")
    public String showResetForm(@RequestParam("token") String token, Model model) {
        PasswordResetToken resetToken = tokenDAO.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            model.addAttribute("message", "Token không hợp lệ hoặc đã hết hạn.");
            return "error";
        }
        model.addAttribute("token", token);
        return "client/auth/reset-password";
    }

    @Transactional
    @PostMapping("/reset-password")
    public String handleReset(@RequestParam("token") String token,
                              @RequestParam("password") String password,
                              Model model) {
        PasswordResetToken resetToken = tokenDAO.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            model.addAttribute("message", "Token không hợp lệ hoặc đã hết hạn.");
            return "error";
        }
        String hashPassword = this.passwordEncoder.encode(password);


        User user = resetToken.getUser();
        user.setPassword(hashPassword);
        userDAO.update(user);
        tokenDAO.delete(resetToken);

        model.addAttribute("message", "Mật khẩu đã được đặt lại thành công.");
        return "redirect:/login";
    }
}

