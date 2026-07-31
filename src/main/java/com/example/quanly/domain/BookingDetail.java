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
@Table(name = "booking_detail", uniqueConstraints = @UniqueConstraint(
        name = "uk_active_booking_slot",
        columnNames = {"sub_court_id", "available_time_id", "date", "slot_active"}))
@Data
public class BookingDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private double price;
    private long sale;

    
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

    /**
     * TRUE với slot đang chiếm lịch; NULL khi booking đã hủy.
     * Unique constraint cho phép nhiều NULL nhưng chỉ một TRUE trên cùng slot.
     */
    @Column(name = "slot_active")
    private Boolean slotActive = Boolean.TRUE;

}
