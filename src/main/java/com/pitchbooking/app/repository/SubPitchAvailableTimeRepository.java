package com.pitchbooking.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pitchbooking.app.domain.AvailableTime;
import com.pitchbooking.app.domain.SubPitch;
import com.pitchbooking.app.domain.SubPitchAvailableTime;

@Repository
public interface SubPitchAvailableTimeRepository extends JpaRepository<SubPitchAvailableTime, Long> {
    Optional<SubPitchAvailableTime> findBySubPitchAndAvailableTime(SubPitch subPitch, AvailableTime availableTime);

    /** Returns only time slots explicitly configured for the selected sub-pitch. */
    @Query("""
            SELECT sat.availableTime
            FROM SubPitchAvailableTime sat
            WHERE sat.subPitch = :subPitch
            ORDER BY sat.availableTime.time
            """)
    java.util.List<AvailableTime> findAvailableTimesBySubPitch(@Param("subPitch") SubPitch subPitch);
}
