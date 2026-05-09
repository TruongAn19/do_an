package com.example.quanly.domain.dto;

import com.example.quanly.domain.RentalType;
import com.example.quanly.domain.dto.validation.ValidRentalContext;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@ValidRentalContext
public class CreateRentalRequest {

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    @NotNull(message = "Loại thuê không được để trống")
    private RentalType type;

    @NotNull(message = "Vui lòng chọn thiết bị")
    private Long equipmentId;

    @Min(value = 1, message = "Số lượng phải ít nhất là 1")
    private int quantity;

    // --- DAILY only (validated by @ValidRentalContext) ---
    private int quantityDay;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate rentalDate;

    // --- ON_SITE only (validated by @ValidRentalContext) ---
    private String bookingCode;
}
