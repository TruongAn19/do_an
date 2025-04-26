package com.example.quanly.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "booking_racket")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRacket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Booking booking;
    @ManyToOne
    private Racket racket;
    @Column(name = "quantity")
    private int quantity;
    @Column(name = "rentalDate")
    private LocalDate rentalDate;
    @Column(name = "total")
    private double total;
}
