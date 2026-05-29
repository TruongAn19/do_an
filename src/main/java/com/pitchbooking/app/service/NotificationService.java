package com.pitchbooking.app.service;

import com.pitchbooking.app.domain.Notification;
import com.pitchbooking.app.domain.NotificationType;
import com.pitchbooking.app.domain.User;
import com.pitchbooking.app.domain.dto.NotificationDTO;
import com.pitchbooking.app.repository.NotificationRepository;
import com.pitchbooking.app.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Persists notifications and pushes them over STOMP.
 *
 * <p>Per-user push uses {@code /user/{email}/queue/notifications} so each
 * authenticated client only receives its own; staff fan-out uses the broadcast
 * {@code /topic/staff-notifications}.
 *
 * <p>DB writes happen first (caller's @Transactional), and WS push fires after
 * commit. For now we accept a small window where the row exists but the push
 * was missed — the FE's bell badge re-syncs via {@code unread-count} on focus.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NotificationService {

    public static final String STAFF_TOPIC = "/topic/staff-notifications";
    public static final String USER_QUEUE = "/queue/notifications";

    NotificationRepository notificationRepository;
    UserRepository userRepository;
    SimpMessagingTemplate messagingTemplate;

    @Transactional
    public NotificationDTO create(Long userId, NotificationType type,
                                  String refType, Long refId,
                                  String title, String message) {
        Notification n = new Notification();
        n.setRecipientUserId(userId);
        n.setType(type);
        n.setRefType(refType);
        n.setRefId(refId);
        n.setTitle(title);
        n.setMessage(message);
        n.setRead(false);
        Notification saved = notificationRepository.save(n);
        return NotificationDTO.from(saved);
    }

    public Page<NotificationDTO> listByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository
                .findByRecipientUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationDTO::from);
    }

    public int countUnread(Long userId) {
        return notificationRepository.countByRecipientUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public boolean markRead(Long notificationId, Long userId) {
        return notificationRepository.markRead(notificationId, userId) > 0;
    }

    @Transactional
    public int markAllRead(Long userId) {
        return notificationRepository.markAllRead(userId);
    }

    public List<Long> staffAndAdminUserIds() {
        return notificationRepository.findStaffAndAdminUserIds();
    }

    // ---- WS push ----

    /** Push to a single user. Resolves email from userId so frontend can subscribe by principal. */
    public void pushToUser(Long userId, NotificationDTO dto) {
        try {
            User u = userRepository.findUserById(userId);
            if (u == null || u.getEmail() == null) {
                log.warn("pushToUser: no user/email for id={} — skipping WS push", userId);
                return;
            }
            messagingTemplate.convertAndSendToUser(u.getEmail(), USER_QUEUE, dto);
        } catch (Exception e) {
            log.warn("pushToUser failed for id={}: {}", userId, e.getMessage());
        }
    }

    /** Broadcast to all staff/admin (per D0.6). */
    public void pushToStaff(NotificationDTO dto) {
        try {
            messagingTemplate.convertAndSend(STAFF_TOPIC, dto);
        } catch (Exception e) {
            log.warn("pushToStaff failed: {}", e.getMessage());
        }
    }
}
