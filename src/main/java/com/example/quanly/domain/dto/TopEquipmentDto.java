package com.example.quanly.domain.dto;

import lombok.Data;

@Data
public class TopEquipmentDto {
    private Long id;
    private String name;
    private double price;
    private String factory;
    private String image;
    private int rentalStock;
    private int rentCount;
    private double revenue;
}
