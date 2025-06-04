package com.example.quanly.repository;

import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.BookingDetail;
import com.example.quanly.domain.SubCourt;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingDetailRepository extends JpaRepository<BookingDetail, Long> {
    Optional<BookingDetail> findBySubCourtAndAvailableTimeAndDate(SubCourt subCourt, AvailableTime time,
                                                                  LocalDate bookingDate);

    List<BookingDetail> findBySubCourtAndDate(SubCourt court, LocalDate date);

    @Query("SELECT bd.product.name, SUM(bd.price - bd.sale) " +
            "FROM BookingDetail bd " +
            "JOIN bd.booking b " +
            "WHERE bd.date BETWEEN :start AND :end " +
            "AND b.status = 'Đã thanh toán' " +
            "GROUP BY bd.product.name")
    List<Object[]> getRevenuePerProductBetweenDates(@Param("start") LocalDate start, @Param("end") LocalDate end);


    List<BookingDetail> findByBookingId(long id);

    @Query("""
                SELECT bd.product.id
                FROM BookingDetail bd
                WHERE MONTH(bd.booking.bookingDate) = :month
                  AND YEAR(bd.booking.bookingDate) = :year
                GROUP BY bd.product.id
                ORDER BY COUNT(bd.id) DESC
            """)
    List<Long> findTop4ProductIdsByMonth(@Param("year") int year, @Param("month") int month, Pageable pageable);

}
