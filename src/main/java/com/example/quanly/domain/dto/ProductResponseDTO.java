package com.example.quanly.domain.dto;

import lombok.Data;

@Data
public class ProductResponseDTO {
    private long id;
    private String name;
    private double price;
    private String image;
    private String detailDesc;
    private String shortDesc;
    private long quantity;
    private long sale;
    private String address;
    private double depositPrice;
    private String status;
    private String ownerName;
}
