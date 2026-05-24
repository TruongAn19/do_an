package com.example.quanly.repository;

import com.example.quanly.domain.Booking;
import com.example.quanly.domain.BookingStatus;
import com.example.quanly.domain.BookingType;
import com.example.quanly.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUser(User user);

    // Tìm các Booking có ít nhất một BookingDetail với ngày cụ thể
    @Query(value = "SELECT DISTINCT b FROM Booking b JOIN b.bookingDetails bd WHERE bd.date = :date", countQuery = "SELECT COUNT(DISTINCT b) FROM Booking b JOIN b.bookingDetails bd WHERE bd.date = :date")
    Page<Booking> findByBookingDetailsDate(@Param("date") LocalDate date, Pageable pageable);

    Booking findByBookingCode(String bookingCode);

    @Query("SELECT b FROM Booking b WHERE b.status = :status and b.bookingDate = :date")
    List<Booking> findBookingsByStatusAndDate(BookingStatus status, LocalDate date);

    List<Booking> findByBookingCodeContainingIgnoreCase(String bookingCode);

    Page<Booking> findAll(Pageable pageable);

    Page<Booking> findByBookingCodeContainingIgnoreCase(String code, Pageable pageable);

    @EntityGraph(attributePaths = {"bookingDetails", "user", "bookingDetails.product", "bookingDetails.availableTime", "bookingDetails.subCourt"})
    Page<Booking> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"bookingDetails", "user", "bookingDetails.product", "bookingDetails.availableTime", "bookingDetails.subCourt"})
    Page<Booking> findByUserIdAndBookingType(Long userId, BookingType bookingType, Pageable pageable);

    @Query(value = "SELECT available_time_id FROM booking WHERE user_id = :userId GROUP BY available_time_id ORDER BY COUNT(*) DESC LIMIT 1", nativeQuery = true)
    Long findMostFrequentTimeSlotByUserId(@Param("userId") Long userId);

    List<Booking> findAllByStatus(BookingStatus status);

}
