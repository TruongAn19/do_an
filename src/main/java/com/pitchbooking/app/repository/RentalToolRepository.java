package com.pitchbooking.app.repository;

import com.pitchbooking.app.domain.RefundStatus;
import com.pitchbooking.app.domain.RentalTool;
import com.pitchbooking.app.domain.RentalToolStatus;
import com.pitchbooking.app.domain.RentalType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RentalToolRepository extends JpaRepository<RentalTool, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT rental FROM RentalTool rental WHERE rental.id = :id")
    Optional<RentalTool> findByIdWithLock(@Param("id") Long id);

    List<RentalTool> findRentalToolsByBookingId(String id);

    List<RentalTool> findByType(RentalType type);

    @Query("SELECT COUNT(r) FROM RentalTool r WHERE r.type = com.pitchbooking.app.domain.RentalType.DAILY AND r.rentalDate BETWEEN :startDate AND :endDate AND r.status IN (com.pitchbooking.app.domain.RentalToolStatus.RENTING, com.pitchbooking.app.domain.RentalToolStatus.COMPLETED) AND (:courtId IS NULL OR r.productId = :courtId)")
    int countDailyRentalByCourtAndDateRange(@Param("courtId") Long courtId, @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(r) FROM RentalTool r WHERE r.type = com.pitchbooking.app.domain.RentalType.DAILY AND r.rentalDate BETWEEN :startDate AND :endDate AND r.status IN (com.pitchbooking.app.domain.RentalToolStatus.RENTING, com.pitchbooking.app.domain.RentalToolStatus.COMPLETED) AND (:courtId IS NULL OR r.productId = :courtId) AND r.equipmentId = :equipmentId")
    int countEquipmentDailyRentalByCourtAndDateRange(@Param("equipmentId") Long equipmentId, @Param("courtId") Long courtId,
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // lay doanh thu trong khoảng thời gian
    @Query("SELECT COALESCE(SUM(r.rentalPrice), 0) FROM RentalTool r WHERE r.type = com.pitchbooking.app.domain.RentalType.DAILY AND r.rentalDate BETWEEN :startDate AND :endDate AND r.paymentStatus = com.pitchbooking.app.domain.RentalPaymentStatus.PAID AND r.status <> com.pitchbooking.app.domain.RentalToolStatus.CANCELLED AND (:courtId IS NULL OR r.productId = :courtId)")
    double sumDailyRevenueByCourtAndDateRange(@Param("courtId") Long courtId, @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT rkt, SUM(rt.quantity), SUM(CASE WHEN rt.paymentStatus = com.pitchbooking.app.domain.RentalPaymentStatus.PAID THEN rt.rentalPrice ELSE 0.0 END) FROM RentalTool rt JOIN Equipment rkt ON rt.equipmentId = rkt.id WHERE rt.type = com.pitchbooking.app.domain.RentalType.DAILY AND rt.rentalDate BETWEEN :startDate AND :endDate AND rt.status IN (com.pitchbooking.app.domain.RentalToolStatus.RENTING, com.pitchbooking.app.domain.RentalToolStatus.COMPLETED) AND (:courtId IS NULL OR rkt.product.id = :courtId) GROUP BY rkt ORDER BY SUM(rt.quantity) DESC")
    List<Object[]> findTopDailyRentedEquipments(@Param("courtId") Long courtId, @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate, Pageable pageable);

    List<RentalTool> findByRentalToolCodeContainingIgnoreCase(String rentalToolCode);

    Page<RentalTool> findRentalByUserId(Long id, Pageable pageable);

    List<RentalTool> findByStatusIn(List<RentalToolStatus> status);

    Page<RentalTool> findByRefundStatus(RefundStatus refundStatus, Pageable pageable);

    Page<RentalTool> findByRefundStatusIn(List<RefundStatus> refundStatuses, Pageable pageable);

    Page<RentalTool> findByType(RentalType type, Pageable pageable);

    Page<RentalTool> findByRentalToolCodeContaining(String code, Pageable pageable);

    @Query("""
                SELECT r.equipmentId
                FROM RentalTool r
                WHERE MONTH(r.rentalDate) = :month
                  AND YEAR(r.rentalDate) = :year
                  AND r.status IN (
                      com.pitchbooking.app.domain.RentalToolStatus.RENTING,
                      com.pitchbooking.app.domain.RentalToolStatus.COMPLETED
                  )
                  AND r.equipmentId IS NOT NULL
                GROUP BY r.equipmentId
                ORDER BY SUM(r.quantity) DESC
            """)
    List<Long> findTop4EquipmentIdsByMonth(@Param("year") int year, @Param("month") int month, Pageable pageable);

}
