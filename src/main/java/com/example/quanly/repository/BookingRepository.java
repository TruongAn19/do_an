package com.example.quanly.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.quanly.domain.Booking;
import com.example.quanly.domain.User;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>{

    List<Booking> findByUser(User user);
    // Tìm các Booking có ít nhất một BookingDetail với ngày cụ thể
    @Query(
            value = "SELECT DISTINCT b FROM Booking b JOIN b.bookingDetails bd WHERE bd.date = :date",
            countQuery = "SELECT COUNT(DISTINCT b) FROM Booking b JOIN b.bookingDetails bd WHERE bd.date = :date"
    )
    Page<Booking> findByBookingDetailsDate(@Param("date") LocalDate date, Pageable pageable);

    Booking findByBookingCode(String bookingCode);


    @Query("SELECT b FROM Booking b WHERE b.status = :status and b.bookingDate = :date")
    List<Booking> findBookingsByStatusAndDate(String status, LocalDate date);

    List<Booking> findByBookingCodeContainingIgnoreCase(String bookingCode);

    Page<Booking> findAll(Pageable pageable);

    Page<Booking> findByBookingCodeContainingIgnoreCase(String code, Pageable pageable);

    Page<Booking> findByUserId(Long userId, Pageable pageable);
}
