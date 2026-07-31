package com.example.quanly;

import com.example.quanly.config.JwtTokenProvider;
import com.example.quanly.config.SecurityConfiguration;
import com.example.quanly.domain.RentalType;
import com.example.quanly.domain.RentalTool;
import com.example.quanly.domain.RentalToolStatus;
import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.PaymentRequest;
import com.example.quanly.domain.dto.VnpayResponse;
import com.example.quanly.repository.RentalToolRepository;
import com.example.quanly.service.PaymentService;
import com.example.quanly.service.RateLimitService;
import com.example.quanly.service.RentalToolService;
import com.example.quanly.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

import com.example.quanly.controller.client.RentalController;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
                .andExpect(jsonPath("$.data.racketId").exists());
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
                  "racketId": 1,
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
    void whenDailyRentalDateIsPast_thenBadRequest() throws Exception {
        String body = """
                {
                  "fullName": "Test",
                  "email": "test@example.com",
                  "phone": "0901234567",
                  "type": "DAILY",
                  "racketId": 1,
                  "quantity": 1,
                  "quantityDay": 1,
                  "rentalDate": "2000-01-01"
                }
                """;
        mockMvc.perform(post("/api/v1/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.rentalDate").exists());
    }

    @Test
    @DisplayName("POST /api/v1/rentals/{id}/pay thiếu paymentMethod trả 400")
    @WithMockUser
    void whenPayMissingPaymentMethod_thenBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/rentals/1/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.paymentMethod").exists());
    }

    @Test
    @DisplayName("VNPay cho đơn DAILY sử dụng tiền cọc vợt, không sử dụng phí thuê")
    @WithMockUser
    void whenPayDailyRentalByVnpay_thenUseRacketDeposit() throws Exception {
        User user = new User();
        user.setId(10L);

        RentalTool rentalTool = new RentalTool();
        rentalTool.setId(1L);
        rentalTool.setUserId(10L);
        rentalTool.setType(RentalType.DAILY);
        rentalTool.setStatus(RentalToolStatus.PENDING);
        rentalTool.setPrice(3_000_000D);
        rentalTool.setRentalPrice(150_000D);

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(rentalToolRepository.findById(1L)).thenReturn(Optional.of(rentalTool));
        when(paymentService.createVnPayPayment(any(PaymentRequest.class), any(HttpServletRequest.class)))
                .thenReturn(VnpayResponse.builder().paymentUrl("https://payment.test").build());

        mockMvc.perform(post("/api/v1/rentals/1/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentMethod\":\"VNPAY\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentUrl").value("https://payment.test"));

        ArgumentCaptor<PaymentRequest> captor = ArgumentCaptor.forClass(PaymentRequest.class);
        verify(paymentService).createVnPayPayment(captor.capture(), any(HttpServletRequest.class));
        assertEquals(3_000_000D, captor.getValue().getAmount());
    }
}
