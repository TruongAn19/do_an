package com.example.quanly.service;

import com.example.quanly.domain.MatchPost;
import com.example.quanly.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    public void notifyNewParticipant(User recipient, User participant, MatchPost post) {
        String message = String.format("%s đã tham gia bài đăng của bạn vào %s",
                participant.getFullName(),
                post.getPlayDate());

        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(),
                "/queue/notifications",
                message);
    }

    public void notifyPostCancelled(User recipient, MatchPost post) {
        String message = String.format("Bài đăng vào %s đã bị huỷ bởi người tạo",
                post.getPlayDate());

        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(),
                "/queue/notifications",
                message);
    }

    public void notifyParticipantLeft(User recipient, User participant, MatchPost post) {
        String message = String.format("%s đã rời khỏi bài đăng của bạn vào %s",
                participant.getFullName(),
                post.getPlayDate());

        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(),
                "/queue/notifications",
                message);
    }
}
