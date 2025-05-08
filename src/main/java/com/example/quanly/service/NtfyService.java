package com.example.quanly.service;

import com.example.quanly.domain.Booking;
import com.example.quanly.domain.User;
import com.example.quanly.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@EnableScheduling
public class NtfyService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserService userService;
    @Autowired
    private BookingRepository bookingRepository;

    /**
     * Gửi thông báo đến một topic cụ thể
     */
    public boolean sendNotification(String topic, String message, String title) {
        try {
            String url = "https://ntfy.sh/" + topic;
            HttpHeaders headers = new HttpHeaders();
            if (title != null && !title.isEmpty()) {
                headers.add("Title", title);
            }
            headers.setContentType(MediaType.TEXT_PLAIN);

            // In ra để kiểm tra giá trị message trước khi gửi
            System.out.println("Gửi thông báo: " + message);

            HttpEntity<String> request = new HttpEntity<>(message, headers);
            restTemplate.postForEntity(url, request, String.class);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi thông báo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Scheduled task để kiểm tra và gửi thông báo định kỳ
     */
    @Scheduled(cron = "0 */30 * * * *") // Mỗi 30 phút
    public void checkAndSendNotifications() {
        List<Booking> bookings = bookingRepository.findBookingsByStatusAndDate("Đã đặt", LocalDate.now());
        LocalDateTime now = LocalDateTime.now();
        System.out.print("===================================");
        for (Booking booking : bookings) {
            LocalTime startTime = booking.getAvailableTime().getTime();
            if (startTime != null) {
                // Gắn LocalTime với ngày hôm nay
                LocalDateTime bookingStartDateTime = LocalDateTime.of(LocalDate.now(), startTime);

                long minutesUntilStart = Duration.between(now, bookingStartDateTime).toMinutes();

                if (minutesUntilStart <= 60 && minutesUntilStart > 0) {
                    String message = "You have a badminton match coming up.";
                    String topic = "user-" + booking.getUser().getId();
                    String title = "Appointment Notification";

                    boolean sent = sendNotification(topic, message, title);
                    if (sent) {
                        System.out.println("Đã gửi thông báo đến topic: " + topic);
                    } else {
                        System.err.println("Không thể gửi thông báo đến: " + topic);
                    }
                }
            }
        }

    }
}