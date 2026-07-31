package com.pitchbooking.app.controller.client;

import com.pitchbooking.app.domain.PasswordResetToken;
import com.pitchbooking.app.domain.User;
import com.pitchbooking.app.domain.utility.PasswordResetTokenDAO;
import com.pitchbooking.app.domain.utility.UserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordControllerTest {

    @Mock UserDAO userDAO;
    @Mock PasswordResetTokenDAO tokenDAO;
    @Mock JavaMailSender mailSender;
    @Mock PasswordEncoder passwordEncoder;

    private ForgotPasswordController controller;

    @BeforeEach
    void setUp() {
        controller = new ForgotPasswordController(userDAO, tokenDAO, mailSender, passwordEncoder);
        ReflectionTestUtils.setField(
                controller,
                "resetPasswordUrl",
                "https://frontend.example/reset-password");
    }

    @Test
    @DisplayName("Forgot password ignores client redirectUrl and uses trusted configuration")
    void handleForgot_ignoresClientRedirectUrl() {
        User user = new User();
        user.setEmail("user@example.com");
        when(userDAO.findByEmail(user.getEmail())).thenReturn(user);

        controller.handleForgot(Map.of(
                "email", user.getEmail(),
                "redirectUrl", "https://attacker.example/steal"));

        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenDAO).save(tokenCaptor.capture());

        ArgumentCaptor<SimpleMailMessage> mailCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mailCaptor.capture());

        String body = mailCaptor.getValue().getText();
        assertThat(body)
                .contains("https://frontend.example/reset-password?token="
                        + tokenCaptor.getValue().getToken())
                .doesNotContain("attacker.example");
    }

    @Test
    @DisplayName("Forgot password does not reveal an unknown email")
    void handleForgot_unknownEmail_returnsGenericResponse() {
        String email = "missing@example.com";
        when(userDAO.findByEmail(email)).thenReturn(null);

        var response = controller.handleForgot(Map.of("email", email));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Nếu email tồn tại, liên kết đặt lại mật khẩu sẽ được gửi.");
        verify(tokenDAO, never()).save(org.mockito.ArgumentMatchers.any());
        verify(mailSender, never()).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
    }
}
