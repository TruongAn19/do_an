package com.pitchbooking.app.controller.client;

import com.pitchbooking.app.domain.PasswordResetToken;
import com.pitchbooking.app.domain.User;
import com.pitchbooking.app.domain.dto.ApiResponse;
import com.pitchbooking.app.domain.dto.ResetPasswordRequest;
import com.pitchbooking.app.domain.utility.PasswordResetTokenDAO;
import com.pitchbooking.app.domain.utility.UserDAO;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

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

    @Value("${app.frontend.reset-password-url:http://localhost:4200/reset-password}")
    @NonFinal
    String resetPasswordUrl;

    @Transactional
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> handleForgot(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email không được để trống.");
        }

        User user = userDAO.findByEmail(email);
        if (user == null) {
            // Không tiết lộ email có tồn tại trong hệ thống hay không.
            return forgotPasswordAccepted();
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

        String resetLink = UriComponentsBuilder.fromUriString(resetPasswordUrl)
                .queryParam("token", token)
                .build()
                .toUriString();
        sendEmail(user.getEmail(), resetLink);

        return forgotPasswordAccepted();
    }

    private ResponseEntity<ApiResponse<String>> forgotPasswordAccepted() {
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status(200)
                .message("Nếu email tồn tại, liên kết đặt lại mật khẩu sẽ được gửi.")
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
    public ResponseEntity<ApiResponse<String>> handleReset(
            @Valid @RequestBody ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenDAO.findByToken(request.getToken());
        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token không hợp lệ hoặc đã hết hạn.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userDAO.update(user);
        tokenDAO.delete(resetToken);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status(200).message("Mật khẩu đã được đặt lại thành công.").data(null).build());
    }
}
