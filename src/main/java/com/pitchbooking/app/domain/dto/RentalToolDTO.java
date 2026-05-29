package com.pitchbooking.app.domain.dto;


import lombok.Data;

@Data
public class RentalToolDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String type; //thue kem booking hoac ko
    private String bookingId;
    private String bookingCode;
    private String bookingDate;
    private String bookingTime;
    private String equipmentId;
    private String equipmentName;
    private String productId;
    private double price;
    private double rentalPrice;
    private String status;
    private int quantity;
    private int quantityDay;
    private String rentalDate;
    private String rentalToolCode;
    private String refundStatus;
    private double depositAmount;
    private String cancelledAt;
}
