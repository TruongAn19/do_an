package com.example.quanly.repository;

import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.SubCourt;
import com.example.quanly.domain.TemporaryBooking;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TemporaryBookingRepository extends JpaRepository<TemporaryBooking, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TemporaryBooking t WHERE t.subCourt = :subCourt AND t.availableTime = :time AND t.bookingDate = :date")
    Optional<TemporaryBooking> findBySubCourtAndAvailableTimeAndBookingDateWithLock(
            @Param("subCourt") SubCourt subCourt,
            @Param("time") AvailableTime time,
            @Param("date") LocalDate date);

    @Modifying
    @Transactional
    @Query("DELETE FROM TemporaryBooking t WHERE t.holdStartTime < :expiryTime")
    void deleteExpiredHolds(@Param("expiryTime") LocalDateTime expiryTime);

    default void deleteExpiredHolds() {
        deleteExpiredHolds(LocalDateTime.now().minusMinutes(3));
    }

    @Modifying
    void deleteBySubCourtAndAvailableTimeAndBookingDate(SubCourt subCourt, AvailableTime time, LocalDate date);
}
