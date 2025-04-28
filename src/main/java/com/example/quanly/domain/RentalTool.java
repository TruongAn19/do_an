package com.example.quanly.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
public class RentalTool {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String type; // DAILY or ON_SITE
    private String bookingId;

    private String racketId;
    private String productId;
    private double price;
    private double rentalPrice;
    @Enumerated(EnumType.STRING)
    private RentalToolStatus status;  // PENDING,
    private int quantity;
    private int quantityDay;
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}
