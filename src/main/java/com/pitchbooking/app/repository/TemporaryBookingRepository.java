package com.pitchbooking.app.repository;

import com.pitchbooking.app.domain.AvailableTime;
import com.pitchbooking.app.domain.SubPitch;
import com.pitchbooking.app.domain.TemporaryBooking;
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
    @Query("DELETE FROM TemporaryBooking t WHERE t.holdExpiresAt < :now")
    void deleteExpiredHolds(@Param("now") LocalDateTime now);

    default void deleteExpiredHolds() {
        deleteExpiredHolds(LocalDateTime.now());
    }

    @Modifying
    void deleteBySubPitchAndAvailableTimeAndBookingDate(SubPitch subPitch, AvailableTime time, LocalDate date);
}
