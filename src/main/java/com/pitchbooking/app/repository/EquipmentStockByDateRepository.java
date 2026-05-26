package com.pitchbooking.app.repository;

import com.pitchbooking.app.domain.EquipmentStockByDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface EquipmentStockByDateRepository extends JpaRepository<EquipmentStockByDate, Long> {
    boolean existsByEquipmentIdAndDate(Long id, LocalDate date);

    Optional<EquipmentStockByDate> findByEquipmentIdAndDate(Long equipmentId, LocalDate date);

    @Query("SELECT SUM(rsbd.rentalStock)\n" +
            "FROM EquipmentStockByDate rsbd\n" +
            "JOIN Equipment r ON rsbd.equipmentId = r.id\n" +
            "WHERE rsbd.date = :today\n" +
            " AND r.product.id = :courtId")
    Integer sumRentalStockByCourtAndDate(Long courtId, LocalDate today);

    @Query("Select rs from EquipmentStockByDate  rs where rs.equipmentId =:equipmentId and rs.date=:date")
    EquipmentStockByDate findByEquipmentAndDate(Long equipmentId, LocalDate date);

}
