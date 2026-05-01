package com.example.quanly.domain.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BookingResponseDTO {
    private long id;
    private String bookingCode;
    private double totalPrice;
    private double depositPrice;
    private String receiverName;
    private String receiverAddress;
    private String receiverPhone;
    private String status;
    private LocalDate bookingDate;
    private String rentalToolCode;
    private UserResponseDTO user;
    private String courtName;
    private String time;
    private List<BookingDetailResponseDTO> bookingDetails;
}
