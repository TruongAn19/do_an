package com.pitchbooking.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pitchbooking.app.domain.AvailableTime;
import com.pitchbooking.app.domain.SubPitch;
import com.pitchbooking.app.domain.SubPitchAvailableTime;

@Repository
public interface SubPitchAvailableTimeRepository extends JpaRepository<SubPitchAvailableTime, Long> {
    Optional<SubPitchAvailableTime> findBySubPitchAndAvailableTime(SubPitch subPitch, AvailableTime availableTime);
}
