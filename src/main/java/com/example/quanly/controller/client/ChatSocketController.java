package com.example.quanly.controller.client;

import com.example.quanly.domain.ChatMessage;
import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.ChatMessageDto;
import com.example.quanly.repository.UserRepository;
import com.example.quanly.service.AuthenticationFacade;
import com.example.quanly.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatService chatService;
    private final AuthenticationFacade authenticationFacade;
    private final UserRepository userRepository;

    @MessageMapping("/chat/{postId}/send")
    @SendTo("/topic/chat/{postId}")
    public ChatMessageDto handleWebSocketMessage(
            @DestinationVariable Long postId,
            @Payload ChatMessageDto messageDto,
            Principal principal) {

        try {
            // principal.getName() sẽ là username nếu bạn dùng DAO Authentication
            String username = principal.getName();

            // Tìm User từ username
            User sender = userRepository.findByEmail(username);
            if (sender == null) {
                throw new RuntimeException("Người dùng không tồn tại");
            }

            Long senderId = sender.getId();

            ChatMessage savedMessage = chatService.sendMessage(postId, senderId, messageDto.getContent());

            return new ChatMessageDto(
                    savedMessage.getId(),
                    senderId,
                    sender.getFullName(),
                    savedMessage.getContent(),
                    formatDateTime(savedMessage.getSentAt())
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        return dateTime.format(formatter);
    }

}
