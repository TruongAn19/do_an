package com.example.quanly.repository;

import com.example.quanly.domain.RacketStockByDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface RacketStockByDateRepository extends JpaRepository<RacketStockByDate, Long> {
    boolean existsByRacketIdAndDate(Long id, LocalDate date);

    Optional<RacketStockByDate> findByRacketIdAndDate(Long racketId, LocalDate date);

    List<RacketStockByDate> findByRacketIdAndDateGreaterThanEqual(Long racketId, LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM RacketStockByDate s WHERE s.racketId = :racketId AND s.date = :date")
    Optional<RacketStockByDate> findByRacketIdAndDateForUpdate(
            @Param("racketId") Long racketId, @Param("date") LocalDate date);

    // courtId nullable: null = sum trên tất cả sân.
    @Query("SELECT SUM(rsbd.rentalStock)\n" +
            "FROM RacketStockByDate rsbd\n" +
            "JOIN Racket r ON rsbd.racketId = r.id\n" +
            "WHERE rsbd.date = :today\n" +
            " AND (:courtId IS NULL OR r.product.id = :courtId)")
    Integer sumRentalStockByCourtAndDate(Long courtId, LocalDate today);

}
