package com.example.quanly.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "racket")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Racket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Mã ID của cây vợt

    @Column(nullable = false, length = 100)
    private String name;  // Tên của cây vợt

    private double price;  // Giá của cây vợt (giá mua)

    private boolean available = true;
    
    private String factory;

    private String image;

    private double rentalPricePerDay;  // Giá cho thuê mỗi ngày

    private double rentalPricePerPlay; //

    private int bookingStockQuantity;  // Số lượng vợt cho thuê theo booking

    private int quantity; // số vợt cho thuê

    private String status;

    @ManyToOne(fetch = FetchType.EAGER)
    private Product product;
}

