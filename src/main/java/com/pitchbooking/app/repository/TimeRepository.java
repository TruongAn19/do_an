package com.pitchbooking.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pitchbooking.app.domain.AvailableTime;

public interface TimeRepository extends JpaRepository<AvailableTime, Long> {
    AvailableTime getById(long timeId);
}