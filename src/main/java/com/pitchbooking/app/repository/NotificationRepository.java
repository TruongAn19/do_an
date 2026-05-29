package com.pitchbooking.app.repository;

import com.pitchbooking.app.domain.Notification;
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

    int countByRecipientUserIdAndIsReadFalse(Long recipientUserId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id AND n.recipientUserId = :userId")
    int markRead(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientUserId = :userId AND n.isRead = false")
    int markAllRead(@Param("userId") Long userId);

    /**
     * IDs of users whose role.name is ADMIN or STAFF — destination for refund-request fan-out.
     */
    @Query("SELECT u.id FROM User u WHERE u.role.name IN ('ADMIN', 'STAFF')")
    List<Long> findStaffAndAdminUserIds();
}
