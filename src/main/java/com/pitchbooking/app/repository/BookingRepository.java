package com.pitchbooking.app.repository;

import com.pitchbooking.app.domain.Booking;
import com.pitchbooking.app.domain.BookingStatus;
import com.pitchbooking.app.domain.RefundStatus;
import com.pitchbooking.app.domain.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUser(User user);

    // Tìm các Booking có ít nhất một BookingDetail với ngày cụ thể
    @Query(value = "SELECT DISTINCT b FROM Booking b JOIN b.bookingDetails bd WHERE bd.date = :date", countQuery = "SELECT COUNT(DISTINCT b) FROM Booking b JOIN b.bookingDetails bd WHERE bd.date = :date")
    Page<Booking> findByBookingDetailsDate(@Param("date") LocalDate date, Pageable pageable);

    Booking findByBookingCode(String bookingCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.bookingCode = :bookingCode")
    Optional<Booking> findByBookingCodeWithLock(@Param("bookingCode") String bookingCode);

    @Query("SELECT b FROM Booking b WHERE b.status = :status and b.bookingDate = :date")
    List<Booking> findBookingsByStatusAndDate(BookingStatus status, LocalDate date);

    List<Booking> findByBookingCodeContainingIgnoreCase(String bookingCode);

    Page<Booking> findAll(Pageable pageable);

    Page<Booking> findByBookingCodeContainingIgnoreCase(String code, Pageable pageable);

    @EntityGraph(attributePaths = {"bookingDetails", "user", "bookingDetails.product", "bookingDetails.availableTime", "bookingDetails.subPitch"})
    Page<Booking> findByUserId(Long userId, Pageable pageable);

    @Query(value = "SELECT available_time_id FROM booking WHERE user_id = :userId GROUP BY available_time_id ORDER BY COUNT(*) DESC LIMIT 1", nativeQuery = true)
    Long findMostFrequentTimeSlotByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingDate = :date")
    long countByBookingDate(@Param("date") LocalDate date);

    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.status = :status AND b.bookingDate >= :startDate AND b.bookingDate <= :endDate")
    Double sumTotalPriceByStatusAndDateBetween(@Param("status") BookingStatus status, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT bd.product.name, COUNT(bd) as count, SUM(bd.price) as revenue FROM BookingDetail bd GROUP BY bd.product.name ORDER BY count DESC")
    List<Object[]> findTopProducts(Pageable pageable);

    @Query("SELECT b FROM Booking b ORDER BY b.id DESC")
    List<Booking> findRecentBookings(Pageable pageable);

    List<Booking> findAllByStatus(BookingStatus status);

    // Refund management (admin)
    Page<Booking> findByRefundStatus(RefundStatus refundStatus, Pageable pageable);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);
}
