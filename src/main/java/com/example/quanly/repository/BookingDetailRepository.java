package com.example.quanly.repository;

import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.BookingDetail;
import com.example.quanly.domain.BookingStatus;
import com.example.quanly.domain.SubCourt;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingDetailRepository extends JpaRepository<BookingDetail, Long> {
  @Query("SELECT COUNT(bd) > 0 FROM BookingDetail bd WHERE bd.subCourt = :subCourt " +
      "AND bd.date >= :fromDate AND bd.booking.status <> com.example.quanly.domain.BookingStatus.DA_HUY")
  boolean existsActiveFromDateBySubCourt(
      @Param("subCourt") SubCourt subCourt,
      @Param("fromDate") LocalDate fromDate);
  Optional<BookingDetail> findBySubCourtAndAvailableTimeAndDate(SubCourt subCourt, AvailableTime time,
      LocalDate bookingDate);

  /**
   * Collision check: chỉ trả về BookingDetail thuộc booking ĐANG ACTIVE (chưa bị huỷ).
   * Dùng khi user đặt sân mới — slot của booking đã huỷ phải được free.
   */
  @Query("SELECT bd FROM BookingDetail bd WHERE bd.subCourt = :subCourt AND bd.availableTime = :time AND bd.date = :date AND bd.booking.status <> com.example.quanly.domain.BookingStatus.DA_HUY")
  Optional<BookingDetail> findActiveBySubCourtAndAvailableTimeAndDate(
      @Param("subCourt") SubCourt subCourt,
      @Param("time") AvailableTime time,
      @Param("date") LocalDate date);

  List<BookingDetail> findBySubCourtAndDate(SubCourt court, LocalDate date);

  @Query("SELECT bd.product.name, SUM(bd.price - bd.sale) " +
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

  /**
   * A1: khi booking bị huỷ phải xoá active_slot_key của mọi BookingDetail thuộc booking đó,
   * để slot được giải phóng (NULL được phép trùng) — nếu không, đặt lại slot sẽ dính UNIQUE.
   */
  @Modifying
  @Query("UPDATE BookingDetail bd SET bd.activeSlotKey = NULL WHERE bd.booking.id = :bookingId")
  void clearActiveSlotKeyByBooking(@Param("bookingId") long bookingId);

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
