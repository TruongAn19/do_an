package com.pitchbooking.app.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "equipment")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    private double price;

    private boolean available = true;

    private String factory;

    private String image;

    private double rentalPricePerDay;

    private double rentalPricePerPlay;

    private int bookingStockQuantity;

    private int quantity;

    private String status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnoreProperties("equipments")
    private Product product;
}

