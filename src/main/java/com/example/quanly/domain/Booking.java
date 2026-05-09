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

    public static final String NO_RENTAL = "KHONG_THUE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private double totalPrice;
    private String receiverName;
    private String bookingCode;
    private String receiverAddress;
    private String receiverPhone;
    @Convert(converter = BookingStatusConverter.class)
    @Column(name = "status", length = 255)
    private BookingStatus status;
    private LocalDate bookingDate;
    private double depositPrice;

    @Enumerated(EnumType.STRING)
    private BookingType bookingType = BookingType.ONE_TIME;
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
    private String rentalToolCode = NO_RENTAL;

}
