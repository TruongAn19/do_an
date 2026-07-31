package com.example.quanly.repository;

import com.example.quanly.domain.PendingBookingPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PendingBookingPaymentRepository extends JpaRepository<PendingBookingPayment, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PendingBookingPayment p WHERE p.id = :id")
    Optional<PendingBookingPayment> findByIdForUpdate(@Param("id") Long id);

    @Transactional
    void deleteByExpiresAtBefore(LocalDateTime expiry);
}
