package com.example.quanly.repository;

import java.time.LocalDate;
import java.util.List;

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
    @Query("SELECT DISTINCT b FROM Booking b JOIN b.bookingDetails bd WHERE bd.date = :date")
    List<Booking> findByBookingDetailsDate(@Param("date") LocalDate date);

    Booking findByBookingCode(String bookingCode);
}
