package com.pitchbooking.app.repository;

import com.pitchbooking.app.domain.EquipmentStockByDate;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EquipmentStockByDateRepository extends JpaRepository<EquipmentStockByDate, Long> {
    boolean existsByEquipmentIdAndDate(Long id, LocalDate date);

    Optional<EquipmentStockByDate> findByEquipmentIdAndDate(Long equipmentId, LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT stock
            FROM EquipmentStockByDate stock
            WHERE stock.equipmentId = :equipmentId
              AND stock.date = :date
            """)
    Optional<EquipmentStockByDate> findByEquipmentIdAndDateWithLock(
            @Param("equipmentId") Long equipmentId,
            @Param("date") LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT stock
            FROM EquipmentStockByDate stock
            WHERE stock.equipmentId = :equipmentId
              AND stock.date >= :fromDate
            ORDER BY stock.date
            """)
    List<EquipmentStockByDate> findFutureStocksWithLock(
            @Param("equipmentId") Long equipmentId,
            @Param("fromDate") LocalDate fromDate);

    @Query("SELECT SUM(rsbd.rentalStock)\n" +
            "FROM EquipmentStockByDate rsbd\n" +
            "JOIN Equipment r ON rsbd.equipmentId = r.id\n" +
            "WHERE rsbd.date = :today\n" +
            " AND r.product.id = :courtId")
    Integer sumRentalStockByCourtAndDate(Long courtId, LocalDate today);

}
