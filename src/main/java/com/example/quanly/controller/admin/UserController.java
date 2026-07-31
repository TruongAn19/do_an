package com.example.quanly.controller.admin;

import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.domain.dto.UserResponseDTO;
import com.example.quanly.service.UploadService;
import com.example.quanly.service.UserService;
import com.example.quanly.util.SecurityUtils;
import com.example.quanly.exception.BusinessConflictException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UploadService uploadService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getUsers() {
        List<UserResponseDTO> users = userService.getAllUser();
        return ResponseEntity.ok(ApiResponse.<List<UserResponseDTO>>builder()
                .status(200).message("Thành công").data(users).build());
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(
            @Valid @RequestPart("user") User user,
            @RequestPart(value = "avatarFile", required = false) MultipartFile file) {

        if (file != null && !file.isEmpty()) {
            user.setAvatar(uploadService.handleSaveUploadFile(file, "avatar"));
        }
        if (user.getRole() == null || user.getRole().getName() == null
                || userService.getRoleByName(user.getRole().getName()) == null) {
            throw new IllegalArgumentException("Role không hợp lệ.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(userService.getRoleByName(user.getRole().getName()));
        UserResponseDTO savedUser = userService.handleSaveUser(user);

        return ResponseEntity.ok(ApiResponse.<UserResponseDTO>builder()
                .status(200).message("Tạo người dùng thành công").data(savedUser).build());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserDetail(@PathVariable long userId) {
        UserResponseDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.<UserResponseDTO>builder()
                .status(200).message("Thành công").data(user).build());
    }

    @PutMapping(value = "/{userId}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(
            @PathVariable long userId,
            @RequestPart("user") User user,
            @RequestPart(value = "avatarFile", required = false) MultipartFile file) {

        User current = userService.updateToUser(userId);
        if (current == null)
            throw new IllegalArgumentException("Không tìm thấy user id=" + userId);

        if (user.getFullName() == null || user.getFullName().isBlank()
                || user.getPhone() == null || user.getPhone().isBlank()) {
            throw new IllegalArgumentException("Họ tên và số điện thoại không được để trống.");
        }
        current.setAddress(user.getAddress());
        current.setFullName(user.getFullName());
        current.setPhone(user.getPhone());
        if (file != null && !file.isEmpty()) {
            current.setAvatar(uploadService.handleSaveUploadFile(file, "avatar"));
        }
        UserResponseDTO updatedUser = userService.handleSaveUser(current);

        return ResponseEntity.ok(ApiResponse.<UserResponseDTO>builder()
                .status(200).message("Cập nhật thành công").data(updatedUser).build());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable long userId) {
        if (securityUtils.getCurrentUser().getId() == userId) {
            throw new BusinessConflictException("Không thể vô hiệu hóa chính tài khoản đang đăng nhập.");
        }
        userService.deleteAUser(userId);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status(200).message("Xóa người dùng thành công").data(null).build());
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateRole(
            @PathVariable long userId,
            @RequestBody Map<String, String> body) {
        User user = userService.updateToUser(userId);
        if (user == null)
            throw new IllegalArgumentException("Không tìm thấy user id=" + userId);
        String roleName = body.get("role");
        if (roleName == null || roleName.isBlank() || userService.getRoleByName(roleName) == null) {
            throw new IllegalArgumentException("Role không hợp lệ.");
        }
        user.setRole(userService.getRoleByName(roleName));
        UserResponseDTO updatedUser = userService.handleSaveUser(user);
        return ResponseEntity.ok(ApiResponse.<UserResponseDTO>builder()
                .status(200).message("Cập nhật role thành công").data(updatedUser).build());
    }
}
