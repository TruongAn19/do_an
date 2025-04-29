package com.example.quanly.domain.dto;

import lombok.Data;

@Data
public class TopRacketDto {
    private Long id;
    private String name;
    private double price;
    private String factory;
    private String image;
    private int rentalStock;
}
