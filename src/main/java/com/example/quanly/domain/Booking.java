package com.example.quanly.domain;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "booking")
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private double totalPrice;
    private String receiverName;
    private String bookingCode;
    private String receiverAddress;
    private String receiverPhone;
    private String status;
    private LocalDate bookingDate;
    private double depositPrice;
    // user id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @OneToMany(mappedBy = "booking")
    private List<BookingDetail> bookingDetails;

    @ManyToOne
    @JoinColumn(name = "available_time_id", nullable = true) // Khóa ngoại trỏ đến AvailableTime
    private AvailableTime availableTime;

    @PrePersist
    public void generateBookingCode() {
        this.bookingCode = "BK" + System.currentTimeMillis();
    }

}
