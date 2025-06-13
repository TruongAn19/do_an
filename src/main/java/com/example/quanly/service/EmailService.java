package com.example.quanly.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EmailService {

    JavaMailSender mailSender;

    public void sendBookingConfirmationEmail(String toEmail, String bookingCode, Long bookingId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Xác nhận đặt sân thành công");

//        String bookingDetailUrl = "http://localhost:8080/booking-history/" + bookingId;
        String bookingDetailUrl = "https://ta-batmintin.store/booking-history/" + bookingId;
        String emailText = "Đặt sân thành công, cảm ơn bạn đã đặt sân.\n"
                + "Mã đặt sân của bạn là: " + bookingCode + "\n"
                + "Xem chi tiết đơn đặt sân tại: " + bookingDetailUrl;

        message.setText(emailText);
        mailSender.send(message);
    }


    public void sendRentalConfirmationEmail(String toEmail, String rentalToolCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setText("Đặt vợt thành công, cảm ơn bạn đã đặt vợt. Mã đặt vợt của bạn là: " + rentalToolCode);
        mailSender.send(message);
    }
}