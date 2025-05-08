package com.example.quanly.repository;

import com.example.quanly.domain.RentalTool;
import com.example.quanly.domain.RentalToolStatus;
import com.example.quanly.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RentalToolRepository extends JpaRepository<RentalTool, Long> {

    List<RentalTool> findRentalToolsByBookingId(String id);

    List<RentalTool> findByType(String tupe);


    // RentalToolRepository
    @Query("SELECT COUNT(r) FROM RentalTool r WHERE r.type = 'DAILY' AND r.rentalDate BETWEEN :startDate AND :endDate AND r.status = 'COMPLETED' AND r.productId = :courtId")
    int countDailyRentalByCourtAndDateRange(@Param("courtId") Long courtId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(r) FROM RentalTool r WHERE r.type = 'DAILY' AND r.rentalDate BETWEEN :startDate AND :endDate AND r.status = 'COMPLETED' AND r.productId = :courtId and r.id = :racketId")
    int countRacketDailyRentalByCourtAndDateRange(@Param("racketId") Long racketId, @Param("courtId") Long courtId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    // lay doanh thu trong khoảng thời gian
    @Query("SELECT COALESCE(SUM(r.rentalPrice), 0) FROM RentalTool r WHERE  r.rentalDate BETWEEN :startDate AND :endDate AND r.status = 'COMPLETED' AND r.productId = :courtId")
    double sumDailyRevenueByCourtAndDateRange(@Param("courtId") Long courtId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT rkt, SUM(rt.quantity) FROM RentalTool rt JOIN Racket rkt ON rt.racketId = rkt.id WHERE rt.type = 'DAILY' AND rt.rentalDate BETWEEN :startDate AND :endDate AND rt.status = 'COMPLETED' AND rkt.product.id = :courtId GROUP BY rkt ORDER BY SUM(rt.quantity) DESC")
    List<Object[]> findTopDailyRentedRackets(@Param("courtId") Long courtId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);


    @Query("SELECT COUNT(rt) FROM RentalTool rt WHERE rt.rentalDate BETWEEN :startDate AND :endDate AND rt.status = :status and rt.productId = :courtId")
    int countByRentalDateBetweenAndStatus(@Param("courtId") Long courtId, @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          @Param("status") RentalToolStatus status);


    // Tính tổng doanh thu trong khoảng thời gian
    @Query("SELECT SUM(rt.rentalPrice) FROM RentalTool rt WHERE rt.rentalDate BETWEEN :startDate AND :endDate AND rt.status = :status and rt.productId = :courtId")
    Double sumRevenueByRentalDateBetweenAndStatus(@Param("courtId") Long courtId, @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate,
                                                  @Param("status") RentalToolStatus status);


    List<RentalTool> findByRentalToolCodeContainingIgnoreCase(String rentalToolCode);

    List<RentalTool> findRentalByUserId(Long id);

    List<RentalTool> findByStatusIn(List<RentalToolStatus> status);
}
