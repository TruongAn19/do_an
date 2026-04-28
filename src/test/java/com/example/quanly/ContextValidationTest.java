package com.example.quanly;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Kiểm thử @ValidRentalContext — cross-field validation theo loại thuê.
 * Validation chặn trước controller nên không cần DB hay mock repository.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContextValidationTest {

    @Autowired
    private MockMvc mockMvc;

    // =========================================================================
    // DAILY — thiếu rentalDate
    // =========================================================================

    @Test
    @DisplayName("DAILY thiếu rentalDate → 400, field error trên rentalDate")
    @WithMockUser
    void whenDailyMissingRentalDate_thenBadRequest() throws Exception {
        String body = """
                {
                  "fullName": "Nguyen Van A",
                  "email": "a@test.com",
                  "phone": "0901234567",
                  "type": "DAILY",
                  "racketId": 1,
                  "quantity": 1,
                  "quantityDay": 2
                }
                """;
        mockMvc.perform(post("/api/v1/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.data.rentalDate").exists());
    }

    // =========================================================================
    // DAILY — quantityDay không được gửi (default 0 < 1)
    // =========================================================================

    @Test
    @DisplayName("DAILY với quantityDay=0 → 400, field error trên quantityDay")
    @WithMockUser
    void whenDailyZeroQuantityDay_thenBadRequest() throws Exception {
        String body = """
                {
                  "fullName": "Nguyen Van A",
                  "email": "a@test.com",
                  "phone": "0901234567",
                  "type": "DAILY",
                  "racketId": 1,
                  "quantity": 1,
                  "rentalDate": "2099-12-01"
                }
                """;
        mockMvc.perform(post("/api/v1/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.data.quantityDay").exists());
    }

    // =========================================================================
    // ON_SITE — thiếu bookingCode
    // =========================================================================

    @Test
    @DisplayName("ON_SITE thiếu bookingCode → 400, field error trên bookingCode")
    @WithMockUser
    void whenOnSiteMissingBookingCode_thenBadRequest() throws Exception {
        String body = """
                {
                  "fullName": "Nguyen Van A",
                  "email": "a@test.com",
                  "phone": "0901234567",
                  "type": "ON_SITE",
                  "racketId": 1,
                  "quantity": 1
                }
                """;
        mockMvc.perform(post("/api/v1/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.data.bookingCode").exists());
    }

    // =========================================================================
    // type không hợp lệ (không phải DAILY / ON_SITE) → 400 deserialize error
    // =========================================================================

    @Test
    @DisplayName("type=INVALID → 400, Jackson không thể deserialize sang RentalType")
    @WithMockUser
    void whenInvalidRentalType_thenBadRequest() throws Exception {
        String body = """
                {
                  "fullName": "A",
                  "email": "a@test.com",
                  "phone": "0901234567",
                  "type": "INVALID_TYPE",
                  "racketId": 1,
                  "quantity": 1
                }
                """;
        mockMvc.perform(post("/api/v1/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST_BODY"));
    }
}
