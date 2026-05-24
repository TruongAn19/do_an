package com.example.quanly.service;

import com.example.quanly.domain.MatchPost;
import com.example.quanly.domain.Notification;
import com.example.quanly.domain.NotificationType;
import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.NotificationDTO;
import com.example.quanly.exception.ResourceNotFoundException;
import com.example.quanly.repository.NotificationRepository;
import com.example.quanly.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String STAFF_TOPIC = "/topic/staff-notifications";
    private static final String USER_QUEUE = "/queue/notifications";

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

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

    // =========================================================================
    // Booking cancel / refund notifications (persist in DB + WS push)
    // =========================================================================

    /**
     * Gửi thông báo cho 1 user cụ thể: lưu DB + push WS tới /user/{email}/queue/notifications.
     * Subscriber-targeted vì WebSocketConfig đã setup STOMP user destination prefix.
     */
    @Transactional
    public NotificationDTO sendToUser(long userId, NotificationType type,
                                      String refType, Long refId,
                                      String title, String message) {
        Notification n = persist(userId, type, refType, refId, title, message);
        NotificationDTO dto = toDTO(n);
        userRepository.findById(userId).ifPresent(u -> {
            try {
                messagingTemplate.convertAndSendToUser(u.getEmail(), USER_QUEUE, dto);
            } catch (Exception ex) {
                log.warn("WS push to user {} failed: {}", u.getEmail(), ex.getMessage());
            }
        });
        return dto;
    }

    /**
     * Fan-out tới mọi user role ADMIN hoặc STAFF: 1 Notification row per recipient (cho inbox riêng)
     * + 1 broadcast lên /topic/staff-notifications (cho real-time).
     */
    @Transactional
    public void sendToAdminStaff(NotificationType type, String refType, Long refId,
                                 String title, String message) {
        List<Long> recipientIds = notificationRepository.findStaffAndAdminUserIds();
        if (recipientIds.isEmpty()) {
            log.warn("Không có user role ADMIN/STAFF nào để gửi notification.");
            return;
        }
        Notification last = null;
        for (Long uid : recipientIds) {
            last = persist(uid, type, refType, refId, title, message);
        }
        if (last != null) {
            NotificationDTO dto = toDTO(last);
            dto.setId(null); // generic notification, không gắn id row cụ thể
            try {
                messagingTemplate.convertAndSend(STAFF_TOPIC, dto);
            } catch (Exception ex) {
                log.warn("WS broadcast to staff failed: {}", ex.getMessage());
            }
        }
    }

    private Notification persist(long userId, NotificationType type, String refType, Long refId,
                                 String title, String message) {
        Notification n = new Notification();
        n.setRecipientUserId(userId);
        n.setType(type);
        n.setRefType(refType);
        n.setRefId(refId);
        n.setTitle(title);
        n.setMessage(message);
        n.setRead(false);
        return notificationRepository.save(n);
    }

    // ----- Query / read -----

    public Page<NotificationDTO> listByUser(long userId, Pageable pageable) {
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toDTO);
    }

    public long countUnread(long userId) {
        return notificationRepository.countByRecipientUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markRead(long notificationId, long userId) {
        int updated = notificationRepository.markRead(notificationId, userId);
        if (updated == 0) {
            throw new ResourceNotFoundException(
                    "Không tìm thấy notification id=" + notificationId + " của user này.");
        }
    }

    @Transactional
    public int markAllRead(long userId) {
        return notificationRepository.markAllRead(userId);
    }

    private NotificationDTO toDTO(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .type(n.getType() != null ? n.getType().name() : null)
                .refType(n.getRefType())
                .refId(n.getRefId())
                .title(n.getTitle())
                .message(n.getMessage())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
