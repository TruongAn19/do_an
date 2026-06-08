package com.example.quanly.controller;

import com.example.quanly.config.VnpayConfig;
import com.example.quanly.config.VnpayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;

@Slf4j
@RestController
@RequestMapping("/api/v1/mock-payment")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "payment.mock.enabled", havingValue = "true")
public class MockPaymentController {

    private final VnpayConfig vnpayConfig;

    @Value("${payment.mock.backendCallbackUrl:http://localhost:8080/api/v1/payments/vnpay-callback}")
    private String backendCallbackUrl;

    @GetMapping(produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> showPaymentPage(
            @RequestParam String amount,
            @RequestParam String orderInfo,
            @RequestParam String returnUrl,
            @RequestParam String txnRef) {

        long amountVnd = Long.parseLong(amount) / 100;
        // returnUrl luôn cố định — không truyền qua link để tránh lỗi encoding
        String confirmBase = "/api/v1/mock-payment/confirm"
                + "?txnRef=" + encode(txnRef)
                + "&orderInfo=" + encode(orderInfo)
                + "&returnUrl=" + encode(returnUrl)
                + "&amount=" + encode(amount);
        String safeOrderInfo = orderInfo.replace("%", "%%");

        String html = """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                  <meta charset="UTF-8">
                  <title>Mock Payment</title>
                  <style>
                    body{margin:0;font-family:sans-serif;background:#f0f2f5;display:flex;justify-content:center;align-items:center;min-height:100vh}
                    .card{background:white;border-radius:12px;padding:40px 48px;max-width:420px;width:100%%;box-shadow:0 4px 20px rgba(0,0,0,.12);text-align:center}
                    .badge{background:#fff3cd;color:#856404;border-radius:20px;padding:4px 14px;font-size:13px;display:inline-block;margin-bottom:20px}
                    h2{margin:0 0 8px;color:#1a1a2e;font-size:22px}
                    .order{color:#888;font-size:14px;margin-bottom:20px}
                    .amount{font-size:32px;font-weight:700;color:#e74c3c;margin-bottom:28px}
                    .btn{display:block;padding:14px;border-radius:8px;text-decoration:none;font-size:16px;font-weight:600;margin-bottom:12px;transition:.15s}
                    .btn-ok{background:#27ae60;color:white}.btn-ok:hover{background:#229954}
                    .btn-cancel{background:#e74c3c;color:white}.btn-cancel:hover{background:#c0392b}
                  </style>
                </head>
                <body>
                  <div class="card">
                    <span class="badge">🧪 Môi trường thử nghiệm</span>
                    <h2>Xác nhận thanh toán</h2>
                    <div class="order">Mã đơn: <b>%s</b></div>
                    <div class="amount">%,d VND</div>
                    <a href="%s&success=true"  class="btn btn-ok">&#9989; Thanh toán thành công</a>
                    <a href="%s&success=false" class="btn btn-cancel">&#10060; Huỷ thanh toán</a>
                  </div>
                </body>
                </html>
                """.formatted(safeOrderInfo, amountVnd, confirmBase, confirmBase);

        return ResponseEntity.ok(html);
    }

    @GetMapping("/confirm")
    public void confirmPayment(
            @RequestParam String txnRef,
            @RequestParam String orderInfo,
            @RequestParam String returnUrl,
            @RequestParam String success,
            @RequestParam(required = false) String amount,
            HttpServletResponse response) throws IOException {

        TreeMap<String, String> params = new TreeMap<>();
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_ResponseCode", "true".equals(success) ? "00" : "24");
        params.put("vnp_TxnRef", txnRef);
        if (amount != null) {
            params.put("vnp_Amount", amount);
        }

        String hashData = VnpayUtil.getPaymentURL(params, false);
        String secureHash = VnpayUtil.hmacSHA512(vnpayConfig.getSecretKey(), hashData);

        String callbackUrl = returnUrl + (returnUrl.contains("?") ? "&" : "?")
                + VnpayUtil.getPaymentURL(params, true)
                + "&vnp_SecureHash=" + secureHash;

        log.info("Mock payment confirm: success={}, orderInfo={}, returnUrl={}, amount={}", success, orderInfo, returnUrl, amount);
        response.sendRedirect(callbackUrl);
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
