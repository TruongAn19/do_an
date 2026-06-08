package com.example.quanly.repository;

import com.example.quanly.domain.RentalTool;
import com.example.quanly.domain.RentalToolStatus;
import com.example.quanly.domain.RentalType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface RentalToolRepository extends JpaRepository<RentalTool, Long> {

    List<RentalTool> findRentalToolsByBookingId(String id);

    List<RentalTool> findByType(RentalType type);

    // courtId nullable: null = aggregate trên TẤT CẢ sân (dashboard tổng quan), non-null = filter theo sân.
    @Query("SELECT COUNT(r) FROM RentalTool r WHERE r.type = 'DAILY' AND r.rentalDate BETWEEN :startDate AND :endDate AND r.status = 'COMPLETED' AND (:courtId IS NULL OR r.productId = :courtId)")
    int countDailyRentalByCourtAndDateRange(@Param("courtId") Long courtId, @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(r) FROM RentalTool r WHERE r.type = 'DAILY' AND r.rentalDate BETWEEN :startDate AND :endDate AND r.status = 'COMPLETED' AND r.productId = :courtId and r.id = :racketId")
    int countRacketDailyRentalByCourtAndDateRange(@Param("racketId") Long racketId, @Param("courtId") Long courtId,
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // lay doanh thu trong khoảng thời gian (courtId nullable — xem ghi chú phía trên)
    @Query("SELECT COALESCE(SUM(r.rentalPrice), 0) FROM RentalTool r WHERE  r.rentalDate BETWEEN :startDate AND :endDate AND r.status = 'COMPLETED' AND (:courtId IS NULL OR r.productId = :courtId)")
    double sumDailyRevenueByCourtAndDateRange(@Param("courtId") Long courtId, @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT rkt, SUM(rt.quantity) FROM RentalTool rt JOIN Racket rkt ON rt.racketId = rkt.id WHERE rt.type = 'DAILY' AND rt.rentalDate BETWEEN :startDate AND :endDate AND rt.status = 'COMPLETED' AND (:courtId IS NULL OR rkt.product.id = :courtId) GROUP BY rkt ORDER BY SUM(rt.quantity) DESC")
    List<Object[]> findTopDailyRentedRackets(@Param("courtId") Long courtId, @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate, Pageable pageable);

    @Query("SELECT COUNT(rt) FROM RentalTool rt WHERE rt.rentalDate BETWEEN :startDate AND :endDate AND rt.status = :status AND (:courtId IS NULL OR rt.productId = :courtId)")
    int countByRentalDateBetweenAndStatus(@Param("courtId") Long courtId, @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") RentalToolStatus status);

    // Tính tổng doanh thu trong khoảng thời gian (courtId nullable)
    @Query("SELECT SUM(rt.rentalPrice) FROM RentalTool rt WHERE rt.rentalDate BETWEEN :startDate AND :endDate AND rt.status = :status AND (:courtId IS NULL OR rt.productId = :courtId)")
    Double sumRevenueByRentalDateBetweenAndStatus(@Param("courtId") Long courtId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") RentalToolStatus status);

    List<RentalTool> findByRentalToolCodeContainingIgnoreCase(String rentalToolCode);

    Page<RentalTool> findRentalByUserId(Long id, Pageable pageable);

    List<RentalTool> findByStatusIn(List<RentalToolStatus> status);

    // Đơn thuê PENDING quá hạn thanh toán — CHỈ DAILY standalone (ON_SITE cũng ở PENDING nhưng
    // gắn booking, do BookingService cascade lo, không được job tự huỷ).
    List<RentalTool> findByStatusAndTypeAndCreateAtBefore(
            RentalToolStatus status, RentalType type, LocalDateTime threshold);

    Page<RentalTool> findByType(RentalType type, Pageable pageable);

    Page<RentalTool> findByRentalToolCodeContaining(String code, Pageable pageable);

    @Query("""
                SELECT r.racketId
                FROM RentalTool r
                WHERE MONTH(r.rentalDate) = :month
                  AND YEAR(r.rentalDate) = :year
                GROUP BY r.racketId
                ORDER BY COUNT(r.id) DESC
            """)
    List<Long> findTop4RacketIdsByMonth(@Param("year") int year, @Param("month") int month, Pageable pageable);

}
