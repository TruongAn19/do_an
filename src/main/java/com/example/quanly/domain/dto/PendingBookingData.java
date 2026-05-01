package com.example.quanly.domain.dto;

import com.example.quanly.domain.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class PendingBookingData {
    private Long temporaryBookingId;
    private User user;
    private String receiverName;
    private String receiverAddress;
    private String receiverPhone;
    private Product product;
    private AvailableTime availableTime;
    private SubCourt subCourt;
    private LocalDate firstBookingDate;
    private BookingType bookingType;
    private LocalDate recurringEndDate;
    private double totalBookingPrice;
    private double depositPrice;
    private List<SlotData> slots;

    @Data
    @AllArgsConstructor
    public static class SlotData {
        private LocalDate date;
        private double price;
        private long sale;
    }
}
