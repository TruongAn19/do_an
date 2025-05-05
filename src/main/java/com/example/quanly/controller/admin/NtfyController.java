package com.example.quanly.controller.admin;

import com.example.quanly.service.NtfyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;

@RestController
public class NtfyController {

    @Autowired
    private WebClient webClient;

    @Autowired
    private NtfyService ntfyService;

    @GetMapping(value = "/ntfy-sse/{topic}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> proxyNtfyEvents(@PathVariable String topic) {
        System.out.println("Đang khởi tạo SSE cho topic: " + topic);
        return webClient.get()
                .uri("https://ntfy.sh/{topic}/sse", topic)
                .retrieve()
                .bodyToFlux(String.class)
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(5)))
                .doOnSubscribe(s -> System.out.println("SSE subscription bắt đầu cho topic: " + topic))
                .doOnNext(data -> System.out.println("SSE data nhận được: " + data))
                .doOnError(e -> System.err.println("Lỗi khi nhận SSE từ NTFY: " + e.getMessage()));
    }

    /**
     * Endpoint để gửi thông báo thủ công
     */
    @PostMapping("/notify")
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