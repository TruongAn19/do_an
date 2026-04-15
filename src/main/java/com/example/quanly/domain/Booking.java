package com.example.quanly.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

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

    private String bookingType = "ONE_TIME"; // ONE_TIME, WEEKLY_RECURRING
    private LocalDate recurringEndDate;

    @ManyToOne

    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({ "bookings", "matchPosts", "participations", "messages", "products", "password" })
    private User user;

    @OneToMany(mappedBy = "booking")
    @JsonIgnoreProperties("booking")
    private List<BookingDetail> bookingDetails;

    @ManyToOne
    @JoinColumn(name = "available_time_id", nullable = true) // Khóa ngoại trỏ đến AvailableTime
    private AvailableTime availableTime;

    @PrePersist
    public void generateBookingCode() {
        this.bookingCode = "BK" + System.currentTimeMillis();
    }

    @Column(name = "rental_tool_code")
    private String rentalToolCode = "KHONG_THUE";

}
