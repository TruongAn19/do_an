package com.example.quanly.controller.client;

import com.example.quanly.domain.PasswordResetToken;
import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.domain.utility.PasswordResetTokenDAO;
import com.example.quanly.domain.utility.UserDAO;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("/api/v1/auth")
public class ForgotPasswordController {

    UserDAO userDAO;
    PasswordResetTokenDAO tokenDAO;
    JavaMailSender mailSender;
    PasswordEncoder passwordEncoder;

    @Transactional
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> handleForgot(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String redirectUrl = body.get("redirectUrl");
        if (!StringUtils.hasText(redirectUrl)) {
            throw new IllegalArgumentException("Thiếu tham số redirectUrl.");
        }

        User user = userDAO.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("Email không tồn tại!");
        }

        PasswordResetToken existingToken = tokenDAO.findByUser(user);
        if (existingToken != null) {
            tokenDAO.delete(existingToken);
        }

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        tokenDAO.save(resetToken);

        String resetLink = redirectUrl + "?token=" + token;
        sendEmail(user.getEmail(), resetLink);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status(200)
                .message("Liên kết đặt lại mật khẩu đã được gửi đến email.")
                .data(null).build());
    }

    private void sendEmail(String to, String link) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject("Đặt lại mật khẩu");
        mail.setText("Nhấn vào link để đặt lại mật khẩu: \n" + link);
        mailSender.send(mail);
    }

    @Transactional
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> handleReset(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String password = body.get("password");

        PasswordResetToken resetToken = tokenDAO.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token không hợp lệ hoặc đã hết hạn.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(password));
        userDAO.update(user);
        tokenDAO.delete(resetToken);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status(200).message("Mật khẩu đã được đặt lại thành công.").data(null).build());
    }
}
