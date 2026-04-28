package com.example.quanly.domain.dto;

import com.example.quanly.domain.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RentalPaymentRequest {

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;
}
