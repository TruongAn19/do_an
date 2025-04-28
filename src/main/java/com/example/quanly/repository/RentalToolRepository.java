package com.example.quanly.repository;

import com.example.quanly.domain.RentalTool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentalToolRepository extends JpaRepository<RentalTool, Long> {

    List<RentalTool> findRentalToolsByBookingId(String id);
    List<RentalTool> findByType(String tupe);
}
