package com.example.quanly.controller.admin;

import com.example.quanly.service.NtfyService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;

@RestController
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NtfyController {

    WebClient webClient;
    NtfyService ntfyService;

    @GetMapping(value = "/api/v1/ntfy-sse/{topic}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> proxyNtfyEvents(@PathVariable String topic) {
        log.info("Đang khởi tạo SSE cho topic: {}", topic);
        return webClient.get()
                .uri("https://ntfy.sh/{topic}/sse", topic)
                .retrieve()
                .bodyToFlux(String.class)
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(5)))
                .doOnSubscribe(s -> log.info("SSE subscription bắt đầu cho topic: {}", topic))
                .doOnNext(data -> log.info("SSE data nhận được: {}", data))
                .doOnError(e -> log.error("Lỗi khi nhận SSE từ NTFY: {}", e.getMessage()));
    }

    @PostMapping("/api/v1/notify")
    public String sendNotification(@RequestParam String topic,
            @RequestParam String message,
            @RequestParam(required = false) String title) {

        boolean success = ntfyService.sendNotification(topic, message, title);
        if (success) {
            return "Đã gửi thông báo thành công đến topic: " + topic;
        } else {
            return "Không thể gửi thông báo đến topic: " + topic;
        }
    }
}
