package com.pitchbooking.app;

import com.pitchbooking.app.config.JwtTokenProvider;
import com.pitchbooking.app.config.SecurityConfiguration;
import com.pitchbooking.app.repository.RentalToolRepository;
import com.pitchbooking.app.service.PaymentService;
import com.pitchbooking.app.service.NotificationService;
import com.pitchbooking.app.service.RateLimitService;
import com.pitchbooking.app.service.RentalToolService;
import com.pitchbooking.app.util.SecurityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.pitchbooking.app.controller.client.RentalController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test nhẹ dùng @WebMvcTest — chỉ tải web layer, không cần DB hay full context.
 * Phù hợp cho: kiểm tra security contract (401/403) và validation contract (400).
 * Service/Repository được @MockBean — không cần giá trị trả về thật.
 */
@WebMvcTest(RentalController.class)
@Import(SecurityConfiguration.class)
@ActiveProfiles("test")
class ControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    // --- Infrastructure mocks cho security filter chain ---
    @MockBean UserDetailsService userDetailsService;
    @MockBean JwtTokenProvider jwtTokenProvider;
    @MockBean PasswordEncoder passwordEncoder;
    @MockBean RateLimitService rateLimitService;

    // --- Controller dependencies ---
    @MockBean RentalToolService rentalToolService;
    @MockBean RentalToolRepository rentalToolRepository;
    @MockBean PaymentService paymentService;
    @MockBean SecurityUtils securityUtils;
    @MockBean NotificationService notificationService;

    // =========================================================================
    // Security: unauthenticated → 401
    // =========================================================================

    @Test
    @DisplayName("POST /api/v1/rentals không có token → 401 (web layer chặn, không cần DB)")
    void whenCreateRentalWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/rentals/{id}/pay không có token → 401")
    void whenPayRentalWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/rentals/1/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Validation: missing required fields → 400
    // =========================================================================

    @Test
    @DisplayName("POST /api/v1/rentals body rỗng → 400 với errorCode VALIDATION_ERROR")
    @WithMockUser
    void whenCreateRentalEmptyBody_thenBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.data.fullName").exists())
                .andExpect(jsonPath("$.data.equipmentId").exists());
    }

    @Test
    @DisplayName("POST /api/v1/rentals với email sai định dạng → 400")
    @WithMockUser
    void whenCreateRentalInvalidEmail_thenBadRequest() throws Exception {
        String body = """
                {
                  "fullName": "Test",
                  "email": "not-an-email",
                  "phone": "0901234567",
                  "type": "DAILY",
                  "equipmentId": 1,
                  "quantity": 1,
                  "quantityDay": 1,
                  "rentalDate": "2099-12-01"
                }
                """;
        mockMvc.perform(post("/api/v1/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.email").value("Email không hợp lệ"));
    }

    @Test
    @DisplayName("POST /api/v1/rentals/{id}/pay thiếu paymentMethod → 400")
    @WithMockUser
    void whenPayMissingPaymentMethod_thenBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/rentals/1/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.paymentMethod").exists());
    }
}
