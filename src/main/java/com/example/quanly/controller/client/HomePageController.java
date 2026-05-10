package com.example.quanly.controller.client;

import com.example.quanly.domain.*;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.domain.dto.BookingResponseDTO;
import com.example.quanly.domain.dto.ProductResponseDTO;
import com.example.quanly.domain.dto.RentalToolDTO;
import com.example.quanly.domain.dto.UserResponseDTO;
import com.example.quanly.repository.BookingDetailRepository;
import com.example.quanly.repository.RentalToolRepository;
import com.example.quanly.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.YearMonth;
import java.util.*;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class HomePageController {

        ProductService productService;
        UserService userService;
        PasswordEncoder passwordEncoder;
        UploadService uploadService;
        BookingService bookingService;
        RentalToolRepository rentalToolRepository;
        RentalToolService rentalToolService;
        EquipmentService equipmentService;
        BookingDetailRepository bookingDetailRepository;

        @GetMapping("/api/v1/client/home")
        public ResponseEntity<ApiResponse<Map<String, Object>>> getHomePage(
                        @RequestParam(value = "page", defaultValue = "1") int page) {

                Pageable pageable = PageRequest.of(page - 1, 4);
                Page<ProductResponseDTO> mainProducts = productService.getAllProductClient(pageable);
                Page<Equipment> byProducts = equipmentService.getAllEquipment(pageable);

                YearMonth currentMonth = YearMonth.now();
                YearMonth previousMonth = currentMonth.minusMonths(1);

                List<Long> topProductIds = bookingDetailRepository.findTop4ProductIdsByMonth(
                                currentMonth.getYear(), currentMonth.getMonthValue(), PageRequest.of(0, 4));
                if (topProductIds.isEmpty()) {
                        topProductIds = bookingDetailRepository.findTop4ProductIdsByMonth(
                                        previousMonth.getYear(), previousMonth.getMonthValue(), PageRequest.of(0, 4));
                }
                List<ProductResponseDTO> topProducts = topProductIds.stream()
                                .map(id -> productService.fetchProductById(id))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .filter(p -> !"DELETED".equals(p.getStatus()))
                                .collect(Collectors.toList());

                List<Long> topEquipmentIds = rentalToolRepository.findTop4EquipmentIdsByMonth(
                                currentMonth.getYear(), currentMonth.getMonthValue(), PageRequest.of(0, 4));
                if (topEquipmentIds.isEmpty()) {
                        topEquipmentIds = rentalToolRepository.findTop4EquipmentIdsByMonth(
                                        previousMonth.getYear(), previousMonth.getMonthValue(), PageRequest.of(0, 4));
                }
                List<Equipment> topEquipments = topEquipmentIds.stream()
                                .map(id -> equipmentService.getEquipmentById(id))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .filter(r -> !"DELETED".equals(r.getStatus()))
                                .collect(Collectors.toList());

                Map<String, Object> data = Map.of(
                                "products", mainProducts.getContent(),
                                "equipments", byProducts.getContent(),
                                "topProducts", topProducts,
                                "topEquipments", topEquipments,
                                "currentPage", page,
                                "totalPages", mainProducts.getTotalPages());

                return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                                .status(200).message("Thành công").data(data).build());
        }

        @GetMapping("/api/v1/client/booking-history")
        public ResponseEntity<ApiResponse<Map<String, Object>>> getBookingHistory(
                        Principal principal,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "5") int size) {

                long userId = userService.getUserByEmail(principal.getName()).getId();
                // Sắp xếp mới nhất lên đầu (theo ID giảm dần)
                Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
                Page<BookingResponseDTO> bookingsPage = bookingService.fetchBookingByUserWithPaging(userId, pageable);

                Map<String, Object> data = Map.of(
                                "bookings", bookingsPage.getContent(),
                                "currentPage", bookingsPage.getNumber(),
                                "totalPages", bookingsPage.getTotalPages());
                return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                                .status(200).message("Thành công").data(data).build());
        }

        @GetMapping("/api/v1/client/booking-history/{id}")
        public ResponseEntity<ApiResponse<Map<String, Object>>> getBookingHistoryDetail(
                        @PathVariable Long id) {
                BookingResponseDTO booking = bookingService.fetchBookingById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy booking id=" + id));
                List<RentalTool> rentalTools = rentalToolRepository.findRentalToolsByBookingId(String.valueOf(id));
                Map<String, Object> data = Map.of(
                                "booking", booking,
                                "bookingDetails", booking.getBookingDetails(),
                                "rentalTools", rentalTools);
                return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                                .status(200).message("Thành công").data(data).build());
        }

        @GetMapping("/api/v1/client/rental-history")
        public ResponseEntity<ApiResponse<Map<String, Object>>> getRentalHistory(
                        Principal principal,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "5") int size) {

                User user = userService.getUserByEmail(principal.getName());
                // Sắp xếp đơn thuê mới nhất lên đầu (theo ID giảm dần)
                Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
                Page<RentalToolDTO> rentals = rentalToolService.fetchRentalByUser(user, pageable);

                Map<String, Object> data = Map.of(
                                "rentals", rentals.getContent(),
                                "totalPages", rentals.getTotalPages(),
                                "currentPage", rentals.getNumber());
                return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                                .status(200).message("Thành công").data(data).build());
        }

        @GetMapping("/api/v1/client/profile")
        public ResponseEntity<ApiResponse<UserResponseDTO>> getProfile(Principal principal) {
                UserResponseDTO user = userService.getUserDTOByEmail(principal.getName());
                return ResponseEntity.ok(ApiResponse.<UserResponseDTO>builder()
                                .status(200).message("Thành công").data(user).build());
        }

        @PutMapping(value = "/api/v1/client/profile", consumes = "multipart/form-data")
        public ResponseEntity<ApiResponse<UserResponseDTO>> updateProfile(
                        @RequestPart("user") User user,
                        @RequestPart(value = "avatarFile", required = false) MultipartFile file,
                        Principal principal) {

                long id = userService.getUserByEmail(principal.getName()).getId();
                User updateUser = userService.updateToUser(id);
                if (file != null && !file.isEmpty()) {
                        updateUser.setAvatar(uploadService.handleSaveUploadFile(file, "avatar"));
                }
                updateUser.setFullName(user.getFullName());
                updateUser.setEmail(user.getEmail());
                updateUser.setAddress(user.getAddress());
                updateUser.setPhone(user.getPhone());
                UserResponseDTO savedUser = userService.handleSaveUser(updateUser);

                return ResponseEntity.ok(ApiResponse.<UserResponseDTO>builder()
                                .status(200).message("Cập nhật thành công").data(savedUser).build());
        }

        @PutMapping("/api/v1/client/change-password")
        public ResponseEntity<ApiResponse<String>> changePassword(
                        @RequestParam("oldPassword") String oldPassword,
                        @RequestParam("newPassword") String newPassword,
                        @RequestParam("confirmPassword") String confirmPassword,
                        Principal principal,
                        HttpServletRequest request,
                        HttpServletResponse response) {

                User currentUser = userService.findByEmail(principal.getName());

                if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
                        throw new IllegalArgumentException("Mật khẩu cũ không đúng");
                }
                if (!newPassword.equals(confirmPassword)) {
                        throw new IllegalArgumentException("Xác nhận mật khẩu không khớp");
                }

                currentUser.setPassword(passwordEncoder.encode(newPassword));
                userService.handleSaveUser(currentUser);

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null) {
                        new SecurityContextLogoutHandler().logout(request, response, auth);
                }

                return ResponseEntity.ok(ApiResponse.<String>builder()
                                .status(200).message("Đổi mật khẩu thành công. Đang đăng xuất...").data(null).build());
        }
}
