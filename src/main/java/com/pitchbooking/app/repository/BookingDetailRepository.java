package com.pitchbooking.app.repository;

import com.pitchbooking.app.domain.AvailableTime;
import com.pitchbooking.app.domain.BookingDetail;
import com.pitchbooking.app.domain.BookingStatus;
import com.pitchbooking.app.domain.SubPitch;
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
  Optional<BookingDetail> findBySubPitchAndAvailableTimeAndDate(SubPitch subPitch, AvailableTime time,
      LocalDate bookingDate);

  List<BookingDetail> findBySubPitchAndDate(SubPitch court, LocalDate date);

  @Query("SELECT bd.date, SUM(bd.price) " +
      "FROM BookingDetail bd " +
      "JOIN bd.booking b " +
      "WHERE bd.date BETWEEN :start AND :end " +
      "AND b.status IN :statuses " +
      "GROUP BY bd.date")
  List<Object[]> getDailyRevenueBetweenDates(
      @Param("start") LocalDate start,
      @Param("end") LocalDate end,
      @Param("statuses") java.util.Collection<BookingStatus> statuses);

  @Query("SELECT bd.product.name, SUM(bd.price - bd.sale) " +
      "FROM BookingDetail bd " +
      "JOIN bd.booking b " +
      "WHERE bd.date BETWEEN :start AND :end " +
      "AND b.status IN :statuses " +
      "GROUP BY bd.product.name")
  List<Object[]> getProductRevenueBetweenDates(
      @Param("start") LocalDate start,
      @Param("end") LocalDate end,
      @Param("statuses") java.util.Collection<BookingStatus> statuses);

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
