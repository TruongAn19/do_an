package com.example.quanly.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.SubPitch;
import com.example.quanly.domain.SubPitchAvailableTime;

@Repository
public interface SubPitchAvailableTimeRepository extends JpaRepository<SubPitchAvailableTime, Long> {
    Optional<SubPitchAvailableTime> findBySubPitchAndAvailableTime(SubPitch subPitch, AvailableTime availableTime);
}
