package com.example.quanly.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VnpayResponse {
    private String code;
    private String message;
    private String paymentUrl;
}
