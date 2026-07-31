package com.example.quanly;

import com.example.quanly.config.VnpayConfig;
import com.example.quanly.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    private final PaymentService paymentService = new PaymentService(mock(VnpayConfig.class));
    private final HttpServletRequest request = mock(HttpServletRequest.class);

    @Test
    void callbackAmountMustMatchExpectedBackendAmount() {
        when(request.getParameter("vnp_Amount")).thenReturn("15000000");

        assertTrue(paymentService.hasExpectedAmount(request, 150_000D));
        assertFalse(paymentService.hasExpectedAmount(request, 149_000D));
    }

    @Test
    void malformedOrMissingCallbackAmountIsRejected() {
        when(request.getParameter("vnp_Amount"))
                .thenReturn(null)
                .thenReturn("")
                .thenReturn("not-a-number");

        assertFalse(paymentService.hasExpectedAmount(request, 150_000D));
        assertFalse(paymentService.hasExpectedAmount(request, 150_000D));
        assertFalse(paymentService.hasExpectedAmount(request, 150_000D));
    }
}
