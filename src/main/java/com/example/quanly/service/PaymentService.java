package com.example.quanly.service;


import com.example.quanly.config.VnpayConfig;
import com.example.quanly.config.VnpayUtil;
import com.example.quanly.domain.RentalTool;
import com.example.quanly.domain.RentalToolStatus;
import com.example.quanly.domain.dto.PaymentRequest;
import com.example.quanly.domain.dto.VnpayResponse;
import com.example.quanly.repository.RentalToolRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@EnableScheduling
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PaymentService {

    VnpayConfig vnpayConfig;
    private final RentalToolRepository rentalToolRepository;

    @Transactional
    public VnpayResponse createVnPayPayment(PaymentRequest paymentRequest, HttpServletRequest request) {
        // Lấy thông tin từ PaymentRequest

        String bankCode = request.getParameter("bankCode");
        long amount = (long) paymentRequest.getAmount();
        // Lưu transactionId cho giao dịch Payment
        String transactionId = VnpayUtil.getRandomNumber(8); // Tạo một transactionId ngẫu nhiên
        // Tạo Map các tham số cho VNPay
        Map<String, String> vnpParamsMap = vnpayConfig.getVNPayConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount *100));
        vnpParamsMap.put("vnp_OrderInfo", paymentRequest.getId() + "-" + paymentRequest.getType());
        vnpParamsMap.put("vnp_TxnRef", transactionId); // Mã giao dịch
        vnpParamsMap.put("vnp_IpAddr", VnpayUtil.getIpAddress(request));

        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }

        // Tạo URL thanh toán
        String queryUrl = VnpayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VnpayUtil.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VnpayUtil.hmacSHA512(vnpayConfig.getSecretKey(), hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;

        String paymentUrl = vnpayConfig.getVnp_PayUrl() + "?" + queryUrl;

        // Trả về VnpayResponse với URL thanh toán
        return VnpayResponse.builder()
                .code("00") // Thành công
                .message("Tạo thanh toán thành công")
                .paymentUrl(paymentUrl)
                .build();
    }





}

