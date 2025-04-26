package com.example.quanly.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

    @Column(nullable = false)
    private double price;  // Giá của cây vợt (giá mua)

    @Column(nullable = false)
    private boolean available = true;  
    
    private String factory;
    private String image;
    @Column(nullable = false)
    private double rentalPrice;

    @Column(nullable = false)
    private int quantity;

    @ManyToOne
    private Product product;
}

