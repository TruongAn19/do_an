package com.pitchbooking.app.domain.dto;

import com.pitchbooking.app.domain.PaymentType;
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