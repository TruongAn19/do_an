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

@Entity
@Table(name = "booking_detail")
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getSale() {
        return sale;
    }

    public void setSale(long sale) {
        this.sale = sale;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public AvailableTime getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(AvailableTime availableTime) {
        this.availableTime = availableTime;
    }

    public SubCourt getSubCourt() {
        return subCourt;
    }

    public void setSubCourt(SubCourt subCourt) {
        this.subCourt = subCourt;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    
    
}
