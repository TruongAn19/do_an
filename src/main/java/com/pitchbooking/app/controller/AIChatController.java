package com.pitchbooking.app.controller;

import com.pitchbooking.app.service.ai.AIChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.chat.enabled", havingValue = "true")
public class AIChatController {

    private final AIChatService aiChatService;

    public record AIChatRequest(
            @Pattern(regexp = "[A-Za-z0-9_-]{1,100}",
                    message = "chatId chỉ được chứa chữ, số, gạch ngang hoặc gạch dưới")
            String chatId,

            @NotBlank(message = "Nội dung câu hỏi không được để trống")
            @Size(max = 2000, message = "Nội dung câu hỏi không được vượt quá 2000 ký tự")
            String message) {
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(
            @Valid @RequestBody AIChatRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        boolean signedIn = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        boolean admin = signedIn && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        String scope = signedIn
                ? "user:" + authentication.getName()
                : "anonymous:" + httpRequest.getRemoteAddr();
        String chatId = request.chatId() == null || request.chatId().isBlank()
                ? "default"
                : request.chatId();

        return aiChatService.chatStream(scope, chatId, request.message(), admin);
    }
}
