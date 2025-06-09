package com.example.quanly.service;

import com.example.quanly.domain.MatchPost;
import com.example.quanly.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    public void notifyPostCancelled(User recipient, MatchPost post) {
        String message = String.format("Bài đăng vào %s đã bị huỷ bởi người tạo",
                post.getPlayDate());

        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(),
                "/queue/notifications",
                message);
    }

    public void notifyNewParticipant(User recipient, User participant, MatchPost post) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", post.getId());
        payload.put("message", String.format("%s đã tham gia bài đăng của bạn vào %s",
                participant.getFullName(),
                post.getPlayDate()));

        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(),
                "/queue/notifications",
                payload);  // Gửi payload JSON thay vì message thô
    }


    public void notifyParticipantLeft(User recipient, User participant, MatchPost post) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", post.getId());
        payload.put("message", String.format("%s đã rời khỏi bài đăng của bạn vào %s",
                participant.getFullName(),
                post.getPlayDate()));

        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(),
                "/queue/notifications",
                payload);
    }

    public void notifyUserKicked(User kickedUser, MatchPost post) {
        String message = String.format("Bạn đã bị loại khỏi bài đăng vào %s", post.getPlayDate());

        messagingTemplate.convertAndSendToUser(
                kickedUser.getEmail(),
                "/queue/notifications",
                message);
    }


}
