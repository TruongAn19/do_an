package com.example.quanly.service;

import com.example.quanly.domain.Booking;
import com.example.quanly.domain.BookingStatus;
import com.example.quanly.repository.BookingRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
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
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NtfyService {

    RestTemplate restTemplate;
    BookingRepository bookingRepository;

    @Async
    public boolean sendNotification(String topic, String message, String title) {
        try {
            String url = "https://ntfy.sh/" + topic;
            HttpHeaders headers = new HttpHeaders();
            if (title != null && !title.isEmpty()) {
                headers.add("Title", title);
            }
            headers.setContentType(MediaType.TEXT_PLAIN);

            log.info("Gửi thông báo: {}", message);

            HttpEntity<String> request = new HttpEntity<>(message, headers);
            restTemplate.postForEntity(url, request, String.class);
            return true;
        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo: {}", e.getMessage());
            return false;
        }
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void checkAndSendNotifications() {
        List<Booking> bookings = bookingRepository.findBookingsByStatusAndDate(BookingStatus.DA_DAT, LocalDate.now());
        LocalDateTime now = LocalDateTime.now();
        log.info("===================================");
        for (Booking booking : bookings) {
            LocalTime startTime = booking.getAvailableTime().getTime();
            if (startTime != null) {
                LocalDateTime bookingStartDateTime = LocalDateTime.of(LocalDate.now(), startTime);

                long minutesUntilStart = Duration.between(now, bookingStartDateTime).toMinutes();

                if (minutesUntilStart <= 60 && minutesUntilStart > 0) {
                    String message = "You have a football match coming up.";
                    String topic = "user-" + booking.getUser().getId();
                    String title = "Appointment Notification";

                    boolean sent = sendNotification(topic, message, title);
                    if (sent) {
                        log.info("Đã gửi thông báo đến topic: {}", topic);
                    } else {
                        log.error("Không thể gửi thông báo đến: {}", topic);
                    }
                }
            }
        }
    }
}
