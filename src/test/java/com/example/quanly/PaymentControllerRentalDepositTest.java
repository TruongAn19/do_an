package com.example.quanly;

import com.example.quanly.controller.PaymentController;
import com.example.quanly.domain.RentalTool;
import com.example.quanly.repository.RentalToolRepository;
import com.example.quanly.service.BookingService;
import com.example.quanly.service.EmailService;
import com.example.quanly.service.PaymentService;
import com.example.quanly.service.PendingBookingCache;
import com.example.quanly.service.RentalToolService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerRentalDepositTest {

    @Mock PaymentService paymentService;
    @Mock RentalToolService rentalToolService;
    @Mock RentalToolRepository rentalToolRepository;
    @Mock BookingService bookingService;
    @Mock PendingBookingCache pendingBookingCache;
    @Mock EmailService emailService;

    @Test
    void dailyRentalCallbackValidatesRacketDepositInsteadOfRentalFee() {
        PaymentController controller = new PaymentController(
                paymentService, rentalToolService, rentalToolRepository,
                bookingService, pendingBookingCache, emailService);

        RentalTool rental = new RentalTool();
        rental.setId(1L);
        rental.setPrice(3_000_000D);
        rental.setRentalPrice(150_000D);
        rental.setRentalToolCode("RT1");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("vnp_ResponseCode", "00");
        request.setParameter("vnp_OrderInfo", "1-RENTAL_TOOL");
        request.setParameter("vnp_Amount", "300000000");

        when(paymentService.verifyVnpayCallback(request)).thenReturn(true);
        when(rentalToolRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(paymentService.hasExpectedAmount(request, 3_000_000D)).thenReturn(true);
        when(rentalToolService.confirmDailyRentalPayment(1L)).thenReturn(rental);

        controller.handleVnpayCallback(request);

        verify(paymentService).hasExpectedAmount(request, 3_000_000D);
        verify(rentalToolService).confirmDailyRentalPayment(1L);
    }
}
