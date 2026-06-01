package com.example.quanly.service;

import com.example.quanly.domain.Notification;
import com.example.quanly.domain.NotificationType;
import com.example.quanly.domain.dto.NotificationDTO;
import com.example.quanly.exception.ForbiddenOperationException;
import com.example.quanly.exception.ResourceNotFoundException;
import com.example.quanly.repository.NotificationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NotificationService {

    NotificationRepository notificationRepository;
    SimpMessagingTemplate messagingTemplate;

    /** Tạo + lưu một notification cho 1 người nhận. */
    @Transactional
    public Notification create(Long userId, NotificationType type, String refType, Long refId,
                               String title, String message) {
        Notification n = new Notification();
        n.setRecipientUserId(userId);
        n.setType(type);
        n.setRefType(refType);
        n.setRefId(refId);
        n.setTitle(title);
        n.setMessage(message);
        n.setIsRead(false);
        n.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(n);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDTO> listByUser(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationDTO::from);
    }

    @Transactional(readOnly = true)
    public int countUnread(Long userId) {
        return (int) notificationRepository.countByRecipientUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markRead(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo id=" + notificationId));
        if (!userId.equals(n.getRecipientUserId())) {
            throw new ForbiddenOperationException("Bạn không có quyền với thông báo này.");
        }
        if (Boolean.FALSE.equals(n.getIsRead())) {
            n.setIsRead(true);
            notificationRepository.save(n);
        }
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllReadByUser(userId);
    }

    /** Push realtime tới 1 user cụ thể (/user/{email}/queue/notifications). */
    public void pushToUser(String email, NotificationDTO dto) {
        if (email == null) return;
        try {
            messagingTemplate.convertAndSendToUser(email, "/queue/notifications", dto);
        } catch (Exception e) {
            log.warn("Không thể push notification realtime tới user {}: {}", email, e.getMessage());
        }
    }

    /** Broadcast realtime tới mọi staff/admin đang subscribe (/topic/staff-notifications). */
    public void pushToStaff(NotificationDTO dto) {
        try {
            messagingTemplate.convertAndSend("/topic/staff-notifications", dto);
        } catch (Exception e) {
            log.warn("Không thể broadcast notification staff: {}", e.getMessage());
        }
    }

    /** Danh sách userId của toàn bộ staff/admin (để tạo notification REFUND_REQUEST). */
    @Transactional(readOnly = true)
    public List<Long> staffAndAdminUserIds() {
        return notificationRepository.findStaffAndAdminUserIds();
    }
}
