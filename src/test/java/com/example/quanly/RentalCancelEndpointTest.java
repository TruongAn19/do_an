package com.example.quanly;

import com.example.quanly.domain.Product;
import com.example.quanly.domain.Racket;
import com.example.quanly.domain.RacketStockByDate;
import com.example.quanly.domain.RentalTool;
import com.example.quanly.domain.RentalToolStatus;
import com.example.quanly.domain.RentalType;
import com.example.quanly.domain.User;
import com.example.quanly.repository.NotificationRepository;
import com.example.quanly.repository.ProductRepository;
import com.example.quanly.repository.RacketRepository;
import com.example.quanly.repository.RacketStockByDateRepository;
import com.example.quanly.repository.RentalToolRepository;
import com.example.quanly.repository.UserRepository;
import com.example.quanly.util.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * B2 — Endpoint POST /api/v1/rentals/{id}/cancel.
 *
 * <p>Chạy full stack qua MockMvc (đi qua GlobalExceptionHandler) để assert đúng mã HTTP thật.
 * {@code @WithMockUser} để qua security filter (authenticated), {@code @MockBean SecurityUtils}
 * để điều khiển "user hiện tại" là chủ đơn hay người khác. H2 thật, dọn thủ công ở {@link #cleanup()}.
 * Ca 401 (không token) nằm ở {@code SecurityTests}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser
class RentalCancelEndpointTest {

    @Autowired MockMvc mockMvc;
    @Autowired RentalToolRepository rentalToolRepository;
    @Autowired RacketRepository racketRepository;
    @Autowired RacketStockByDateRepository racketStockByDateRepository;
    @Autowired ProductRepository productRepository;
    @Autowired UserRepository userRepository;
    @Autowired NotificationRepository notificationRepository;

    @MockBean SecurityUtils securityUtils;

    private User owner;
    private User other;
    private Racket racket;

    private static final LocalDate FUTURE = LocalDate.of(2026, 10, 12);

    @BeforeEach
    void seed() {
        owner = newUser("owner-b2@example.com", "0912000001");
        other = newUser("other-b2@example.com", "0912000002");

        Product product = new Product();
        product.setName("San cau long B2");
        product.setPrice(100000);
        product = productRepository.save(product);

        racket = new Racket();
        racket.setName("Yonex Arcsaber");
        racket.setPrice(500000);
        racket.setRentalPricePerPlay(30000);
        racket.setAvailable(true);
        racket.setProduct(product);
        racket.setBookingStockQuantity(5);
        racket = racketRepository.save(racket);
    }

    @AfterEach
    void cleanup() {
        rentalToolRepository.deleteAll();
        racketStockByDateRepository.deleteAll();
        notificationRepository.deleteAll();
        racketRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ------------------------------------------------------------------ helpers

    private User newUser(String email, String phone) {
        User u = new User();
        u.setEmail(email);
        u.setPassword("secret");
        u.setFullName(email);
        u.setPhone(phone);
        return userRepository.save(u);
    }

    private RacketStockByDate stock(LocalDate date, int available, int reserved, int rental) {
        RacketStockByDate s = new RacketStockByDate();
        s.setRacketId(racket.getId());
        s.setDate(date);
        s.setAvailableStock(available);
        s.setReservedStock(reserved);
        s.setRentalStock(rental);
        s.setTotalStock(available + reserved + rental);
        return racketStockByDateRepository.save(s);
    }

    private RentalTool rental(Long userId, RentalToolStatus status) {
        RentalTool rt = new RentalTool();
        rt.setType(RentalType.DAILY);
        rt.setStatus(status);
        rt.setRacketId(racket.getId());
        rt.setUserId(userId);
        rt.setQuantity(1);
        rt.setQuantityDay(1);
        rt.setRentalDate(FUTURE);
        rt.setFullName("B2 Tester");
        rt.setEmail("b2test@example.com");
        rt.setPhone("0912000000");
        rt.setCreateAt(LocalDateTime.now());
        rt.setUpdateAt(LocalDateTime.now());
        return rentalToolRepository.save(rt);
    }

    // ------------------------------------------------------------------ tests

    @Test
    @DisplayName("User huỷ đơn của chính mình → 200 + status CANCELLED + hoàn kho")
    void owner_cancel_succeeds() throws Exception {
        stock(FUTURE, 4, 1, 0);
        RentalTool rt = rental(owner.getId(), RentalToolStatus.IN_USE);
        when(securityUtils.getCurrentUser()).thenReturn(owner);

        mockMvc.perform(post("/api/v1/rentals/" + rt.getId() + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        assertEquals(RentalToolStatus.CANCELLED,
                rentalToolRepository.findById(rt.getId()).orElseThrow().getStatus());
        RacketStockByDate after = racketStockByDateRepository
                .findByRacketIdAndDate(racket.getId(), FUTURE).orElseThrow();
        assertEquals(5, after.getAvailableStock(), "Kho phải được hoàn");
        assertEquals(0, after.getReservedStock());
    }

    @Test
    @DisplayName("User huỷ đơn của người khác → 403, đơn không bị đổi")
    void nonOwner_cancel_forbidden() throws Exception {
        RentalTool rt = rental(owner.getId(), RentalToolStatus.IN_USE);
        when(securityUtils.getCurrentUser()).thenReturn(other);

        mockMvc.perform(post("/api/v1/rentals/" + rt.getId() + "/cancel"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        assertEquals(RentalToolStatus.IN_USE,
                rentalToolRepository.findById(rt.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Huỷ đơn không tồn tại → 404")
    void cancel_notFound() throws Exception {
        when(securityUtils.getCurrentUser()).thenReturn(owner);

        mockMvc.perform(post("/api/v1/rentals/999999/cancel"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Huỷ đơn đã COMPLETED → 409")
    void cancel_completed_conflict() throws Exception {
        RentalTool rt = rental(owner.getId(), RentalToolStatus.COMPLETED);
        when(securityUtils.getCurrentUser()).thenReturn(owner);

        mockMvc.perform(post("/api/v1/rentals/" + rt.getId() + "/cancel"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Owner huỷ lại đơn đã CANCELLED → 200 (idempotent)")
    void owner_cancel_idempotent() throws Exception {
        RentalTool rt = rental(owner.getId(), RentalToolStatus.CANCELLED);
        when(securityUtils.getCurrentUser()).thenReturn(owner);

        mockMvc.perform(post("/api/v1/rentals/" + rt.getId() + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }
}
