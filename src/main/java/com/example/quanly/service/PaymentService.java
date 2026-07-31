package com.example.quanly.service;

import com.example.quanly.config.VnpayConfig;
import com.example.quanly.config.VnpayUtil;
import com.example.quanly.domain.dto.PaymentRequest;
import com.example.quanly.domain.dto.VnpayResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PaymentService {

    VnpayConfig vnpayConfig;

    @Transactional
    public VnpayResponse createVnPayPayment(PaymentRequest paymentRequest, HttpServletRequest request) {
        String bankCode = request.getParameter("bankCode");
        long amount = (long) paymentRequest.getAmount();

        String transactionId = VnpayUtil.getRandomNumber(8);
        Map<String, String> vnpParamsMap = vnpayConfig.getVNPayConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount * 100));
        // OrderInfo format: "{id}-{TYPE}" — callback dùng id để tra cứu từ DB
        vnpParamsMap.put("vnp_OrderInfo", paymentRequest.getId() + "-" + paymentRequest.getType());
        vnpParamsMap.put("vnp_TxnRef", transactionId);
        vnpParamsMap.put("vnp_IpAddr", VnpayUtil.getIpAddress(request));

        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }

        // Chế độ mock: trả về trang thanh toán giả chạy local, không cần VNPay thật
        if (vnpayConfig.isMockEnabled()) {
            String mockUrl = "http://localhost:8080/api/v1/mock-payment"
                    + "?amount=" + vnpParamsMap.get("vnp_Amount")
                    + "&orderInfo=" + URLEncoder.encode(vnpParamsMap.get("vnp_OrderInfo"), StandardCharsets.UTF_8)
                    + "&returnUrl=" + URLEncoder.encode(vnpParamsMap.get("vnp_ReturnUrl"), StandardCharsets.UTF_8)
                    + "&txnRef=" + transactionId;
            return VnpayResponse.builder()
                    .code("00").message("Mock payment").paymentUrl(mockUrl).build();
        }

        String queryUrl = VnpayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VnpayUtil.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VnpayUtil.hmacSHA512(vnpayConfig.getSecretKey(), hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;

        String paymentUrl = vnpayConfig.getVnp_PayUrl() + "?" + queryUrl;

        return VnpayResponse.builder()
                .code("00")
                .message("Tạo thanh toán thành công")
                .paymentUrl(paymentUrl)
                .build();
    }

    /**
     * Xác thực chữ ký HMAC-SHA512 từ VNPay gửi về.
     * Phải gọi trước khi xử lý bất kỳ nghiệp vụ nào trong callback.
     */
    public boolean verifyVnpayCallback(HttpServletRequest request) {
        String vnpSecureHash = request.getParameter("vnp_SecureHash");
        if (vnpSecureHash == null || vnpSecureHash.isBlank()) {
            return false;
        }

        // Thu thập tất cả params ngoại trừ chữ ký, sắp xếp theo key
        Map<String, String> fields = new TreeMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (!"vnp_SecureHash".equals(key) && !"vnp_SecureHashType".equals(key)) {
                fields.put(key, values[0]);
            }
        });

        // Tính lại hash từ params và so sánh (case-insensitive)
        String hashData = VnpayUtil.getPaymentURL(fields, false);
        String calculatedHash = VnpayUtil.hmacSHA512(vnpayConfig.getSecretKey(), hashData);
        return calculatedHash.equalsIgnoreCase(vnpSecureHash);
    }

    /**
     * Đối chiếu số tiền VNPay trả về với số tiền backend đã tính.
     * VNPay biểu diễn số tiền theo đơn vị nhỏ nhất nên giá trị gửi đi được nhân 100.
     */
    public boolean hasExpectedAmount(HttpServletRequest request, double expectedAmount) {
        String callbackAmount = request.getParameter("vnp_Amount");
        if (callbackAmount == null || callbackAmount.isBlank() || expectedAmount < 0) {
            return false;
        }

        try {
            long actualAmount = Long.parseLong(callbackAmount);
            long expectedVnpAmount = Math.multiplyExact((long) expectedAmount, 100L);
            return actualAmount == expectedVnpAmount;
        } catch (NumberFormatException | ArithmeticException e) {
            return false;
        }
    }
}
