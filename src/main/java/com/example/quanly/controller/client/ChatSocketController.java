package com.example.quanly.controller.client;

import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.domain.dto.ChatMessageDto;
import com.example.quanly.repository.UserRepository;
import com.example.quanly.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatService chatService;
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

            return chatService.sendMessage(postId, senderId, messageDto.getContent());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/api/v1/chat/history/{chatRoomId}")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<ChatMessageDto>>> getChatHistory(@PathVariable Long chatRoomId) {
        List<ChatMessageDto> messages = chatService.getMessageDtos(chatRoomId);
        return ResponseEntity.ok(ApiResponse.<List<ChatMessageDto>>builder()
                .status(200).message("Thành công").data(messages).build());
    }

}
