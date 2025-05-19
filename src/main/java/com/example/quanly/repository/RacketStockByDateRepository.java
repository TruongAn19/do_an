package com.example.quanly.repository;

import com.example.quanly.domain.RacketStockByDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface RacketStockByDateRepository extends JpaRepository<RacketStockByDate, Long> {
    boolean existsByRacketIdAndDate(Long id, LocalDate date);

    Optional<RacketStockByDate> findByRacketIdAndDate(Long racketId, LocalDate date);

    @Query("SELECT SUM(rsbd.rentalStock)\n" +
            "FROM RacketStockByDate rsbd\n" +
            "JOIN Racket r ON rsbd.racketId = r.id\n" +
            "WHERE rsbd.date = :today\n" +
            " AND r.product.id = :courtId")
    Integer sumRentalStockByCourtAndDate(Long courtId, LocalDate today);

    @Query("Select rs from RacketStockByDate  rs where rs.racketId =:racketId and rs.date=:date")
    RacketStockByDate findByRacketAndDate(Long racketId, LocalDate date);

}
