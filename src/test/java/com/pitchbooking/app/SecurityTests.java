package com.pitchbooking.app;

import com.pitchbooking.app.domain.RentalTool;
import com.pitchbooking.app.domain.RentalToolStatus;
import com.pitchbooking.app.repository.RentalToolRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Kiểm thử security và validation của REST API.
 *
 * Chiến lược:
 *  - Dùng profile "test" với H2 in-memory, không cần MySQL hay Redis thật.
 *  - Các test security/401/403 không cần DB vì Spring Security chặn trước controller.
 *  - Các test validation/400 không cần DB vì @Valid chặn trước service được gọi.
 *  - @MockBean cho RentalToolRepository ở một số test cần tra cứu entity.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private RentalToolRepository rentalToolRepository;

    // =========================================================================
    // Unauthenticated access → 401
    // =========================================================================

    @Test
    @DisplayName("POST /api/v1/rentals không có token → 401")
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
                        .content("{\"paymentMethod\":\"CASH\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/client/bookings/hold không có token → 401")
    void whenHoldCourtWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/client/bookings/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/client/bookings/place không có token → 401")
    void whenPlaceBookingWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/client/bookings/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Authorization: USER role không được truy cập admin endpoint → 403
    // =========================================================================

    @Test
    @DisplayName("USER role truy cập GET /api/v1/admin/bookings → 403")
    @WithMockUser(roles = "USER")
    void whenUserRoleAccessesAdminBookings_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/bookings"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("USER role truy cập GET /api/v1/admin/users → 403")
    @WithMockUser(roles = "USER")
    void whenUserRoleAccessesAdminUsers_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("USER role không được gửi thông báo Ntfy")
    @WithMockUser(roles = "USER")
    void whenUserRoleSendsNtfy_thenForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/notify")
                        .param("topic", "system")
                        .param("message", "test"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Gửi thông báo Ntfy không có token → 401")
    void whenSendNtfyWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/notify")
                        .param("topic", "system")
                        .param("message", "test"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Validation: thiếu field bắt buộc → 400 với field errors
    // =========================================================================

    @Test
    @DisplayName("POST /api/v1/rentals thiếu field bắt buộc → 400 validation error")
    @WithMockUser
    void whenCreateRentalWithEmptyBody_thenBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Dữ liệu đầu vào không hợp lệ"))
                .andExpect(jsonPath("$.data.fullName").exists())
                .andExpect(jsonPath("$.data.email").exists())
                .andExpect(jsonPath("$.data.equipmentId").exists());
    }

    @Test
    @DisplayName("POST /api/v1/rentals với email không hợp lệ → 400")
    @WithMockUser
    void whenCreateRentalWithInvalidEmail_thenBadRequest() throws Exception {
        String body = """
                {
                  "fullName": "Nguyen Van A",
                  "email": "not-an-email",
                  "phone": "0901234567",
                  "type": "DAILY",
                  "equipmentId": 1,
                  "quantity": 1,
                  "quantityDay": 1,
                  "rentalDate": "2026-05-01"
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
    void whenPayRentalWithMissingPaymentMethod_thenBadRequest() throws Exception {
        RentalTool mockRental = new RentalTool();
        mockRental.setStatus(RentalToolStatus.PENDING);
        mockRental.setUserId(1L);
        when(rentalToolRepository.findById(anyLong())).thenReturn(Optional.of(mockRental));

        mockMvc.perform(post("/api/v1/rentals/1/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.paymentMethod").exists());
    }

    @Test
    @DisplayName("POST /api/v1/client/bookings/place thiếu receiverName → 400")
    @WithMockUser
    void whenPlaceBookingWithMissingFields_thenBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/client/bookings/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.data.receiverName").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register với mật khẩu dưới 6 ký tự → 400")
    void whenRegisterWithShortPassword_thenBadRequest() throws Exception {
        String body = """
                {
                  "firstName": "Nguyen",
                  "lastName": "Van A",
                  "email": "short-password@test.com",
                  "phone": "0901234567",
                  "password": "123",
                  "confirmPassword": "123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.data.password").value("Mật khẩu phải có tối thiểu 6 ký tự"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/reset-password với mật khẩu dưới 6 ký tự → 400")
    void whenResetWithShortPassword_thenBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "unused-token",
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.data.password").value("Mật khẩu phải có tối thiểu 6 ký tự"));
    }

    @Test
    @DisplayName("PUT /api/v1/client/change-password với mật khẩu dưới 6 ký tự → 400")
    @WithMockUser(username = "password-test@test.com", roles = "USER")
    void whenChangeWithShortPassword_thenBadRequest() throws Exception {
        mockMvc.perform(put("/api/v1/client/change-password")
                        .param("oldPassword", "old-password")
                        .param("newPassword", "123")
                        .param("confirmPassword", "123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Mật khẩu phải có tối thiểu 6 ký tự"));
    }

    // =========================================================================
    // VNPay callback: chữ ký không hợp lệ → 400
    // =========================================================================

    @Test
    @DisplayName("GET /api/v1/payments/vnpay-callback không có chữ ký → 400")
    void whenVnpayCallbackWithoutSignature_thenBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/payments/vnpay-callback")
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_OrderInfo", "1-BOOKING"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Chữ ký không hợp lệ"));
    }

    @Test
    @DisplayName("GET /api/v1/payments/vnpay-callback với chữ ký sai → 400")
    void whenVnpayCallbackWithInvalidSignature_thenBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/payments/vnpay-callback")
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_OrderInfo", "1-BOOKING")
                        .param("vnp_SecureHash", "deadbeefdeadbeefdeadbeef"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Chữ ký không hợp lệ"));
    }

    // =========================================================================
    // Bảo mật mật khẩu BCrypt
    // =========================================================================

    @Test
    @DisplayName("BCrypt: mật khẩu được mã hóa và kiểm tra đúng")
    void whenPasswordEncoded_thenMatchesCorrectly() {
        String raw = "P@ssw0rd123";
        String encoded = passwordEncoder.encode(raw);

        assertNotEquals(raw, encoded, "Mật khẩu phải được mã hóa");
        assertTrue(passwordEncoder.matches(raw, encoded), "Mật khẩu đúng phải khớp");
        assertFalse(passwordEncoder.matches("wrongpassword", encoded), "Mật khẩu sai không được khớp");
    }

    // =========================================================================
    // Public endpoints: không cần auth
    // =========================================================================

    @Test
    @DisplayName("GET /api/v1/products (public) → không bị chặn bởi security")
    void whenAccessPublicProductEndpoint_thenNotBlocked() throws Exception {
        // Có thể trả 200 hoặc 5xx nếu service lỗi, nhưng không được là 401/403
        int status = mockMvc.perform(get("/api/v1/products"))
                .andReturn().getResponse().getStatus();
        assertNotEquals(401, status, "Public endpoint không được trả 401");
        assertNotEquals(403, status, "Public endpoint không được trả 403");
    }

    @Test
    @DisplayName("GET /api/v1/payments/vnpay-callback (public) → không bị chặn bởi security")
    void whenAccessVnpayCallback_thenNotBlocked() throws Exception {
        // Endpoint này public (VNPay gọi về), không được bị 401/403 dù không có token
        int status = mockMvc.perform(get("/api/v1/payments/vnpay-callback"))
                .andReturn().getResponse().getStatus();
        assertNotEquals(401, status);
        assertNotEquals(403, status);
    }

    @Test
    @DisplayName("AI chat mặc định bị tắt và không đăng ký endpoint")
    void whenAiChatDisabled_thenEndpointIsNotExposed() throws Exception {
        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"test\"}"))
                .andExpect(status().isNotFound());
    }
}
