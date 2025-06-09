package com.example.quanly;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SecurityTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;

    // 📌 1. Đăng nhập an toàn
    @Test
    @Order(1)
    void testLoginSuccess() throws Exception {
        mockMvc.perform(formLogin("/login")
                .user("username", "testuser")
                .password("password", "correctpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test @Order(2)
    void testPasswordEncodedWithBCrypt() {
        String rawPassword = "mypassword";
        String encoded = passwordEncoder.encode(rawPassword);
        assertNotEquals(rawPassword, encoded);
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
    }

    // 📌 2. XSS
    @Test @Order(3)
    void testXssInputShouldBeEscaped() throws Exception {
        mockMvc.perform(post("/user/profile")
                .with(csrf())
                .param("fullName", "<script>alert('hack')</script>"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>"))));
    }

    // 📌 3. SQL Injection
    @Test @Order(4)
    void testSqlInjectionFails() throws Exception {
        mockMvc.perform(get("/user/search")
                .param("q", "' OR '1'='1"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("user list"))));
    }

    // 📌 4. Phân quyền
    @Test @Order(5)
    @WithMockUser(username = "normal", roles = {"USER"})
    void testUserCannotAccessAdmin() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test @Order(6)
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminCanAccessAdminPage() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk());
    }

    // 📌 6. Đặt sân đồng thời (mô phỏng)
    @Test @Order(7)
    @WithMockUser(username = "user1")
    void testBookingConflict() throws Exception {
        mockMvc.perform(post("/booking")
                .with(csrf())
                .param("courtId", "1")
                .param("time", "10:00"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/booking")
                .with(csrf())
                .param("courtId", "1")
                .param("time", "10:00"))
                .andExpect(content().string(containsString("Đã có người đặt sân này rồi")));
    }

    // 📌 7. Mã hóa thông tin nhạy cảm
    @Test @Order(8)
    void testPasswordHashingWithBCrypt() {
        String raw = "secure123";
        String hashed = passwordEncoder.encode(raw);
        assertTrue(passwordEncoder.matches(raw, hashed));
        assertFalse(hashed.contains(raw));
    }
}