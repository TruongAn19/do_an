package com.example.quanly.domain;


import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "booking_detail")
@Data
public class BookingDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private double price;
    private long sale;

    
    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    // product_id: long
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "available_time_id", nullable = true) // Khóa ngoại trỏ đến AvailableTime
    private AvailableTime availableTime;

    @ManyToOne
    @JoinColumn(name = "sub_court_id", nullable = true)
    private SubCourt subCourt;

    @Column(name = "date")
    private LocalDate date;

}
