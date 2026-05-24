package com.example.quanly.domain.dto;

import com.example.quanly.domain.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class PendingBookingData {
    /**
     * Tất cả TemporaryBooking holds liên quan tới phiên đặt này. ONE_TIME có 1 phần tử;
     * WEEKLY_RECURRING có N phần tử (mỗi ngày trong chu kỳ một hold để chống race).
     */
    private List<Long> temporaryBookingIds;
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
    /** Rackets bundled with this booking (resolved Racket entity + quantity). Empty if no rackets. */
    private List<RentalSlot> rentals;

    @Data
    @AllArgsConstructor
    public static class SlotData {
        private LocalDate date;
        private double price;
        private long sale;
    }

    @Data
    @AllArgsConstructor
    public static class RentalSlot {
        private Racket racket;
        private int quantity;
        private double unitPrice;
        private double subtotal;
    }
}
