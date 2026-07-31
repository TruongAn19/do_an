package com.pitchbooking.app.service;

import com.pitchbooking.app.config.VnpayConfig;
import com.pitchbooking.app.config.VnpayUtil;
import com.pitchbooking.app.domain.dto.PaymentRequest;
import com.pitchbooking.app.domain.dto.VnpayResponse;
import com.pitchbooking.app.domain.PaymentTransaction;
import com.pitchbooking.app.exception.ResourceNotFoundException;
import com.pitchbooking.app.repository.PaymentTransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PaymentService {

    VnpayConfig vnpayConfig;
    PaymentTransactionRepository paymentTransactionRepository;

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

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setTxnRef(transactionId);
        transaction.setPaymentType(paymentRequest.getType());
        transaction.setEntityId(paymentRequest.getId());
        transaction.setExpectedAmount(amount);
        transaction.setOrderInfo(vnpParamsMap.get("vnp_OrderInfo"));
        transaction.setCreatedAt(java.time.LocalDateTime.now());
        paymentTransactionRepository.save(transaction);

        // Chế độ mock: trả về trang thanh toán giả chạy local, không cần VNPay thật
        if (vnpayConfig.isMockEnabled()) {
            String mockUrl = getMockPaymentBaseUrl(request)
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

    private String getMockPaymentBaseUrl(HttpServletRequest request) {
        if (StringUtils.hasText(vnpayConfig.getMockPaymentUrl())) {
            return vnpayConfig.getMockPaymentUrl();
        }

        return ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(request.getContextPath() + "/api/v1/mock-payment")
                .replaceQuery(null)
                .build()
                .toUriString();
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

    @Transactional
    public PaymentTransaction validateCallbackTransaction(HttpServletRequest request) {
        String txnRef = request.getParameter("vnp_TxnRef");
        String orderInfo = request.getParameter("vnp_OrderInfo");
        String amount = request.getParameter("vnp_Amount");
        if (!StringUtils.hasText(txnRef) || !StringUtils.hasText(orderInfo) || !StringUtils.hasText(amount)) throw new IllegalArgumentException("Callback thanh toán thiếu thông tin giao dịch");
        PaymentTransaction transaction = paymentTransactionRepository.findByTxnRefWithLock(txnRef).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch thanh toán"));
        long callbackAmount;
        try { callbackAmount = Long.parseLong(amount); } catch (NumberFormatException ex) { throw new IllegalArgumentException("Số tiền callback không hợp lệ"); }
        if (!transaction.getOrderInfo().equals(orderInfo) || callbackAmount != transaction.getExpectedAmount() * 100) throw new IllegalArgumentException("Thông tin callback không khớp giao dịch đã tạo");
        return transaction;
    }

    @Transactional
    public void recordCallback(PaymentTransaction transaction, String responseCode, boolean successful) {
        transaction.setResponseCode(responseCode); transaction.setSuccessful(successful);
        transaction.setCallbackAt(java.time.LocalDateTime.now()); paymentTransactionRepository.save(transaction);
    }
}
