package com.example.quanly.controller;

import com.example.quanly.service.ai.AIChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.chat.enabled", havingValue = "true")
public class AIChatController {

    private final AIChatService aiChatService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String chatId = request.getOrDefault("chatId", "default-session");
        return aiChatService.chatStream(chatId, message);
    }
}
