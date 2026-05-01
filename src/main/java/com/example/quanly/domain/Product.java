package com.example.quanly.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;


@Entity
@Table(name = "products")

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    private String name;
    private double price;
    private String image;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String detailDesc;
    private String shortDesc;
    private long quantity;
    private long sale;
    private String address;
    private double depositPrice;
    private String status;

    @Transient
    private String subCourtNames;


    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"matchPosts", "participations", "messages", "products", "password"})
    private User user;

    @ManyToMany
    @JoinTable(
        name = "court_time", // Bảng nối
        joinColumns = @JoinColumn(name = "court_id"), // Khóa ngoại tham chiếu đến sân
        inverseJoinColumns = @JoinColumn(name = "time_id") // Khóa ngoại tham chiếu đến thời gian
    )
    private Set<AvailableTime> availableTimes;

    @OneToMany(mappedBy = "product")
    private List<Racket> rackets;



}
