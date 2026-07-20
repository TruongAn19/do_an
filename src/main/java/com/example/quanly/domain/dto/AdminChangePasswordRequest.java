package com.example.quanly.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminChangePasswordRequest(
        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 6, max = 72, message = "Mật khẩu mới phải có từ 6 đến 72 ký tự")
        String newPassword) {
}
