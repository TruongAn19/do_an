package com.example.quanly.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.SubCourt;
import com.example.quanly.domain.SubCourtAvailableTime;

@Repository
public interface SubCourtAvailableTimeRepository extends JpaRepository<SubCourtAvailableTime, Long> {
    Optional<SubCourtAvailableTime> findBySubCourtAndAvailableTime(SubCourt subCourt, AvailableTime availableTime);

}