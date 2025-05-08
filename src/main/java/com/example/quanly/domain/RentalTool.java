package com.example.quanly.domain;

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
    private String type; // DAILY or ON_SITE
    private String bookingId;
    private Long racketId;
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
