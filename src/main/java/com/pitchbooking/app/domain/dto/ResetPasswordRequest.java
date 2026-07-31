package com.pitchbooking.app.domain.dto;

import com.pitchbooking.app.service.validator.PasswordPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Token đặt lại mật khẩu không được để trống")
    private String token;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = PasswordPolicy.MIN_LENGTH, message = PasswordPolicy.MIN_LENGTH_MESSAGE)
    private String password;
}
