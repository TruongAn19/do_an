package com.example.quanly.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pending_booking_payment")
@Data
public class PendingBookingPayment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long temporaryBookingId;
    private Long userId;
    private Long productId;
    private Long availableTimeId;
    private Long subCourtId;
    private String receiverName;
    private String receiverAddress;
    private String receiverPhone;
    private LocalDate firstBookingDate;
    @Enumerated(EnumType.STRING)
    private BookingType bookingType;
    private LocalDate recurringEndDate;
    private double totalBookingPrice;
    private double depositPrice;
    @Lob @Column(columnDefinition = "LONGTEXT")
    private String slotsJson;
    @Lob @Column(columnDefinition = "LONGTEXT")
    private String rentalSlotsJson;
    @Lob @Column(columnDefinition = "LONGTEXT")
    private String temporaryBookingIdsJson;
    private LocalDateTime expiresAt;
    private String processingStatus = "PENDING";
    private Long completedBookingId;
    private String completedBookingCode;
}
