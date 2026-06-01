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

    Page<Notification> findByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId, Pageable pageable);

    long countByRecipientUserIdAndIsReadFalse(Long recipientUserId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientUserId = :userId AND n.isRead = false")
    int markAllReadByUser(@Param("userId") Long userId);

    /** Tất cả userId có role ADMIN hoặc STAFF — để gửi notification cho nhân viên. */
    @Query("SELECT u.id FROM User u WHERE u.role.name IN ('ADMIN', 'STAFF')")
    List<Long> findStaffAndAdminUserIds();
}
