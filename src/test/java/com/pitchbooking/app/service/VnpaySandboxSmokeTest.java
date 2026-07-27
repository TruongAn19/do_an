package com.pitchbooking.app.service;

import com.pitchbooking.app.config.VnpayConfig;
import com.pitchbooking.app.domain.PaymentType;
import com.pitchbooking.app.domain.dto.PaymentRequest;
import com.pitchbooking.app.domain.dto.VnpayResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class VnpaySandboxSmokeTest {

    @Test
    @EnabledIfSystemProperty(named = "vnpay.sandbox.test", matches = "true")
    void configuredSandbox_generatesSignedPaymentPage() throws Exception {
        Properties properties = loadRuntimeProperties();
        VnpayConfig config = buildConfig(properties);
        PaymentService paymentService = new PaymentService(config);

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .id(999_999L)
                .amount(10_000d)
                .type(PaymentType.PENDING_BOOKING)
                .redirectUrl("")
                .build();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRemoteAddr("127.0.0.1");

        VnpayResponse response = paymentService.createVnPayPayment(paymentRequest, servletRequest);

        assertThat(response.getCode()).isEqualTo("00");
        assertThat(response.getPaymentUrl())
                .startsWith(properties.getProperty("payment.vnPay.url"))
                .contains("vnp_TmnCode=")
                .contains("vnp_OrderInfo=999999-PENDING_BOOKING")
                .contains("vnp_SecureHash=");

        String outputFile = System.getProperty("vnpay.sandbox.url.file");
        if (outputFile != null && !outputFile.isBlank()) {
            Files.writeString(Path.of(outputFile), response.getPaymentUrl(), StandardCharsets.UTF_8);
        }
    }

    private Properties loadRuntimeProperties() throws Exception {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IllegalStateException("Không tìm thấy application.properties.");
            }
            properties.load(input);
        }
        return properties;
    }

    private VnpayConfig buildConfig(Properties properties) {
        VnpayConfig config = new VnpayConfig();
        ReflectionTestUtils.setField(config, "vnp_PayUrl",
                required(properties, "payment.vnPay.url"));
        ReflectionTestUtils.setField(config, "vnp_ReturnUrl",
                required(properties, "payment.vnPay.returnUrl"));
        ReflectionTestUtils.setField(config, "vnp_TmnCode",
                required(properties, "payment.vnPay.tmnCode"));
        ReflectionTestUtils.setField(config, "secretKey",
                required(properties, "payment.vnPay.secretKey"));
        ReflectionTestUtils.setField(config, "vnp_Version",
                required(properties, "payment.vnPay.version"));
        ReflectionTestUtils.setField(config, "vnp_Command",
                required(properties, "payment.vnPay.command"));
        ReflectionTestUtils.setField(config, "orderType",
                required(properties, "payment.vnPay.orderType"));
        ReflectionTestUtils.setField(config, "mockEnabled", false);
        ReflectionTestUtils.setField(config, "mockPaymentUrl", "");
        return config;
    }

    private String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Thiếu cấu hình " + key);
        }
        return value.trim();
    }
}
