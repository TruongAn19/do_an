package com.example.quanly.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.BookingDetail;
import com.example.quanly.domain.SubCourt;

@Repository
public interface BookingDetailRepository extends JpaRepository<BookingDetail, Long>{
    Optional<BookingDetail> findBySubCourtAndAvailableTimeAndDate(SubCourt subCourt, AvailableTime time,
            LocalDate bookingDate);

    List<BookingDetail> findBySubCourtAndDate(SubCourt court, LocalDate date);

    @Query("SELECT bd.product.name, SUM(bd.price - bd.sale) " +
            "FROM BookingDetail bd " +
            "WHERE bd.date BETWEEN :start AND :end " +
            "GROUP BY bd.product.name")
    List<Object[]> getRevenuePerProductBetweenDates(@Param("start") LocalDate start, @Param("end") LocalDate end);

}
