package com.pitchbooking.app.domain.dto;

import com.pitchbooking.app.domain.BookingType;
import com.pitchbooking.app.domain.RefundStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private List<RentalToolDTO> rentalTools;

    // CANCEL_BOOKING_FEATURE — surfaced to FE for refund UI
    private BookingType bookingType;
    private RefundStatus refundStatus;
    private Double refundAmount;
    private LocalDateTime cancelledAt;
    private Integer usedSessionsAtCancel;
    private Integer totalSessionsAtCancel;
    private String cancelReason;
}
