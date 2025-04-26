package com.example.quanly.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quanly.domain.BookingRacket;

public interface BookingRacketRepository extends JpaRepository<BookingRacket, Long> {
    
}
