package com.example.quanly.repository;

import com.example.quanly.domain.RacketStockByDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface RacketStockByDateRepository extends JpaRepository<RacketStockByDate, Long> {
    boolean existsByRacketIdAndDate(Long id, LocalDate date);

    Optional<RacketStockByDate> findByRacketIdAndDate(Long racketId, LocalDate date);
    // Custom query methods can be defined here if needed
}
