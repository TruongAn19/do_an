package com.pitchbooking.app.domain;


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
    private String addressDetail;
    private String status;

    @Transient
    private String subPitchNames;

    @Transient
    @Enumerated(EnumType.STRING)
    private PitchType pitchType = PitchType.FIVE_ASIDE;


    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"products", "password"})
    private User user;

    @ManyToMany
    @JoinTable(
        name = "pitch_time",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "time_id")
    )
    private Set<AvailableTime> availableTimes;

    @OneToMany(mappedBy = "product")
    private List<Equipment> equipments;



}
