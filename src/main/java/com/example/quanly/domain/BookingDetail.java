package com.example.quanly.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(name = "booking_detail",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_booking_detail_active_slot",
                columnNames = "active_slot_key"))
@Data
public class BookingDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private double price;
    private long sale;

    // A1: khóa chống trùng slot cho booking ĐANG active; NULL khi booking đã huỷ.
    // NULL được phép trùng (cả MySQL lẫn H2) -> slot đã huỷ vẫn cho đặt lại.
    @Column(name = "active_slot_key", length = 64)
    private String activeSlotKey;

    
    @ManyToOne
    @JoinColumn(name = "booking_id")
    @JsonIgnoreProperties("bookingDetails")
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
