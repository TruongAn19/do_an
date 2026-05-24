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
    private String bookingType;
    private LocalDate recurringEndDate;
    // Refund info (chỉ có giá trị khi booking đã huỷ)
    private String refundStatus;
    private Double refundAmount;
    private java.time.LocalDateTime cancelledAt;
    private Integer usedSessionsAtCancel;
    private Integer totalSessionsAtCancel;
    private String cancelReason;
    private List<BookingDetailResponseDTO> bookingDetails;
}
