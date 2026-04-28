package com.example.quanly.domain.dto;

import com.example.quanly.domain.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentRequest {
    private Long id;
    private PaymentType type;
    private double amount;
    private String redirectUrl;
}