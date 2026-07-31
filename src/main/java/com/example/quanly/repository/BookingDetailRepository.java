package com.example.quanly.repository;

import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.BookingDetail;
import com.example.quanly.domain.BookingStatus;
import com.example.quanly.domain.SubCourt;
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

  /**
   * Tìm slot đã đặt cho (sân phụ, khung giờ, ngày) — BỎ QUA booking đã huỷ (DA_HUY)
   * để slot của booking bị huỷ được giải phóng và có thể đặt lại.
   */
  @Query("SELECT bd FROM BookingDetail bd " +
      "WHERE bd.subCourt = :subCourt AND bd.availableTime = :time AND bd.date = :bookingDate " +
      "AND bd.booking.status <> :excludedStatus")
  Optional<BookingDetail> findActiveBySubCourtAndAvailableTimeAndDate(
      @Param("subCourt") SubCourt subCourt,
      @Param("time") AvailableTime time,
      @Param("bookingDate") LocalDate bookingDate,
      @Param("excludedStatus") BookingStatus excludedStatus);

  default Optional<BookingDetail> findBySubCourtAndAvailableTimeAndDate(SubCourt subCourt, AvailableTime time,
      LocalDate bookingDate) {
    return findActiveBySubCourtAndAvailableTimeAndDate(subCourt, time, bookingDate, BookingStatus.DA_HUY);
  }

  /**
   * Các slot đã đặt cho (sân phụ, ngày) — BỎ QUA booking đã huỷ (DA_HUY).
   */
  @Query("SELECT bd FROM BookingDetail bd " +
      "WHERE bd.subCourt = :court AND bd.date = :date " +
      "AND bd.booking.status <> :excludedStatus")
  List<BookingDetail> findActiveBySubCourtAndDate(
      @Param("court") SubCourt court,
      @Param("date") LocalDate date,
      @Param("excludedStatus") BookingStatus excludedStatus);

  default List<BookingDetail> findBySubCourtAndDate(SubCourt court, LocalDate date) {
    return findActiveBySubCourtAndDate(court, date, BookingStatus.DA_HUY);
  }

  // bd.price đã là giá cuối cùng; bd.sale chỉ lưu metadata tỷ lệ giảm.
  @Query("SELECT bd.product.name, SUM(bd.price) " +
      "FROM BookingDetail bd " +
      "JOIN bd.booking b " +
      "WHERE bd.date BETWEEN :start AND :end " +
      "AND b.status = :status " +
      "GROUP BY bd.product.name")
  List<Object[]> getRevenuePerProductBetweenDates(
      @Param("start") LocalDate start,
      @Param("end") LocalDate end,
      @Param("status") BookingStatus status);

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
