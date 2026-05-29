package com.pitchbooking.app.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
public class RentalTool implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    @Enumerated(EnumType.STRING)
    private RentalType type;
    private String bookingId;
    private Long equipmentId;
    private Long productId;
    private double price;
    private double rentalPrice;
    @Enumerated(EnumType.STRING)
    private RentalToolStatus status;  // PENDING,
    private Integer quantity;
    private Integer quantityDay;
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private String rentalToolCode;

    @Enumerated(EnumType.STRING)
    private RefundStatus refundStatus = RefundStatus.NONE;
    private double depositAmount;
    private LocalDateTime cancelledAt;

    private Long userId;
    // user id
//    @ManyToOne
//    @JoinColumn(name = "user_id")
//    private User user;

    @PrePersist
    public void generateRentalToolCode() {
        this.rentalToolCode = "RT" + System.currentTimeMillis();
    }
}
