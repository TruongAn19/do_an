package com.example.quanly.repository;

import com.example.quanly.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByRecipientUserIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id AND n.recipientUserId = :userId")
    int markRead(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientUserId = :userId AND n.isRead = false")
    int markAllRead(@Param("userId") Long userId);

    /** Tìm user (id) thuộc role ADMIN hoặc STAFF — dùng để fan-out notification. */
    @Query(value = "SELECT u.id FROM user u JOIN roles r ON u.role_id = r.id WHERE r.name IN ('ADMIN', 'STAFF')", nativeQuery = true)
    List<Long> findStaffAndAdminUserIds();
}
