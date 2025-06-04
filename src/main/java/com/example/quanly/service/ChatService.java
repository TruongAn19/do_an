package com.example.quanly.service;

import com.example.quanly.domain.ChatMessage;
import com.example.quanly.domain.MatchPost;
import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.ChatMessageDto;
import com.example.quanly.repository.ChatMessageRepository;
import com.example.quanly.repository.MatchPostRepository;
import com.example.quanly.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final MatchPostRepository matchPostRepository;
    private final UserRepository userRepository;

    public List<ChatMessageDto> getMessageDtos(Long postId) {
        return chatMessageRepository.findByMatchPostIdOrderBySentAtAsc(postId)
                .stream()
                .map(msg -> new ChatMessageDto(
                        msg.getId(),
                        msg.getSender().getId(),
                        msg.getSender().getFullName(),
                        msg.getContent(),
                        msg.getSentAt().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"))
                ))
                .toList();
    }


    @Transactional
    public ChatMessage sendMessage(Long postId, Long senderId, String content) {
        MatchPost post = matchPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatMessage message = new ChatMessage();
        message.setMatchPost(post);
        message.setSender(sender);
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());

        return chatMessageRepository.save(message);
    }
}
