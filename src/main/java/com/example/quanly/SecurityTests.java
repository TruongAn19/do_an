package com.example.quanly;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SecurityTests {

    private static final Logger logger = LoggerFactory.getLogger(SecurityTests.class);

    // Test users
    private static final String TEST_USER = "testuser";
    private static final String TEST_PASSWORD = "correctpassword";
    private static final String ADMIN_USER = "admin";
    private static final String NORMAL_USER = "normal";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @Order(1)
    void testLoginSuccess() throws Exception {
        logger.info("🔐 Testing valid login");

        mockMvc.perform(formLogin("/login")
                .user("username", TEST_USER)
                .password("password", TEST_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        logger.info("✅ Login successful with valid credentials");
    }

    @Test
    @Order(2)
    void testLoginFailure() throws Exception {
        logger.info("🔐 Testing invalid login");

        mockMvc.perform(formLogin("/login")
                .user("username", TEST_USER)
                .password("password", "wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));

        logger.info("✅ Login failed as expected with invalid credentials");
    }

    @Test
    @Order(3)
    void testPasswordEncodedWithBCrypt() {
        logger.info("🔐 Testing password encryption with BCrypt");

        String rawPassword = "mypassword";
        String encoded = passwordEncoder.encode(rawPassword);

        assertAll(
                () -> assertNotEquals(rawPassword, encoded, "Password should be encoded"),
                () -> assertTrue(passwordEncoder.matches(rawPassword, encoded), "Encoded password should match raw password")
        );

        logger.info("✅ Password properly encoded and matched");
    }

    @Test
    @Order(4)
    void testXssInputShouldBeEscaped() throws Exception {
        logger.info("🛡️ Testing XSS protection");

        String xssInput = "<script>alert('hack')</script>";

        MvcResult result = mockMvc.perform(get("/profile")
                .param("fullName", xssInput))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertFalse(responseContent.contains(xssInput), "XSS input should be escaped");

        logger.info("✅ XSS input was properly escaped");
    }

    @Test
    @Order(5)
    void testSqlInjectionFails() throws Exception {
        logger.info("🛡️ Testing SQL injection protection");

        String sqlInjection = "' OR '1'='1";

        MvcResult result = mockMvc.perform(get("/admin/user")
                .param("q", sqlInjection))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertFalse(responseContent.toLowerCase().contains("user list"), "SQL injection should not return user list");

        logger.info("✅ SQL injection attempt was blocked");
    }

    @Test
    @Order(6)
    @WithMockUser(username = NORMAL_USER, roles = {"USER"})
    void testUserCannotAccessAdmin() throws Exception {
        logger.info("🔒 Testing USER role cannot access admin page");

        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());

        logger.info("✅ User properly denied access to admin page");
    }

    @Test
    @Order(7)
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    void testAdminCanAccessAdminPage() throws Exception {
        logger.info("🔓 Testing ADMIN role can access admin page");

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Admin Dashboard"))); // Thêm kiểm tra nội dung cụ thể

        logger.info("✅ Admin successfully accessed admin page");
    }

    @Test
    @Order(8)
    @WithMockUser(username = "user1")
    void testBookingConflict() throws Exception {
        logger.info("🔄 Testing booking conflict handling");

        // First booking should succeed
        mockMvc.perform(post("/api/temp-booking")

                .param("subCourtId", "1")
                .param("availableTimeId", "1")
                .param("bookingDate", "2025-06-13"))
                .andExpect(status().isOk());

        // Second booking for same slot should conflict
        mockMvc.perform(post("/api/temp-booking")

                .param("subCourtId", "1")
                .param("availableTimeId", "1")
                .param("bookingDate", "2025-06-13"))
                .andExpect(status().isConflict());

        logger.info("✅ Booking conflict properly detected");
    }
}