package com.example.quanly.repository;

import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.SubPitch;
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
import java.util.List;
import java.util.Optional;

@Repository
public interface TemporaryBookingRepository extends JpaRepository<TemporaryBooking, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TemporaryBooking t WHERE t.subPitch = :subPitch AND t.availableTime = :time AND t.bookingDate = :date")
    Optional<TemporaryBooking> findBySubPitchAndAvailableTimeAndBookingDateWithLock(
            @Param("subPitch") SubPitch subPitch,
            @Param("time") AvailableTime time,
            @Param("date") LocalDate date);

    List<TemporaryBooking> findBySubPitchAndBookingDate(SubPitch subPitch, LocalDate bookingDate);

    @Modifying
    @Transactional
    @Query("DELETE FROM TemporaryBooking t WHERE t.holdStartTime < :expiryTime")
    void deleteExpiredHolds(@Param("expiryTime") LocalDateTime expiryTime);

    default void deleteExpiredHolds() {
        deleteExpiredHolds(LocalDateTime.now().minusMinutes(3));
    }

    @Modifying
    void deleteBySubPitchAndAvailableTimeAndBookingDate(SubPitch subPitch, AvailableTime time, LocalDate date);
}
