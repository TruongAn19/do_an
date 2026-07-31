package com.example.quanly.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
    
    @NotBlank(message = "Tên sân không được để trống")
    private String name;
    @Positive(message = "Giá sân phải lớn hơn 0")
    private double price;
    private String image;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String detailDesc;
    private String shortDesc;
    @Positive(message = "Số lượng sân phụ phải lớn hơn 0")
    private long quantity;
    @Min(value = 0, message = "Giảm giá không thể âm")
    @Max(value = 100, message = "Giảm giá không thể vượt quá 100%")
    private long sale;
    @NotBlank(message = "Địa chỉ sân không được để trống")
    private String address;
    private String addressDetail;
    @PositiveOrZero(message = "Tiền cọc không thể âm")
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
