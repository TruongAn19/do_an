package com.pitchbooking.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pitchbooking.app.domain.BookingType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * Snapshot of a booking pending payment. Stored in {@link com.pitchbooking.app.service.PendingBookingCache}
 * (Redis-backed in dev/prod, in-memory in test).
 *
 * Holds entity IDs rather than entity instances so it can be serialized
 * to/from Redis without dragging JPA proxies / lazy collections into the cache.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PendingBookingData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long temporaryBookingId;
    private Long userId;
    private String userEmail;
    private String receiverName;
    private String receiverAddress;
    private String receiverPhone;
    private Long productId;
    private Long availableTimeId;
    private Long subPitchId;
    private LocalDate firstBookingDate;
    private BookingType bookingType;
    private LocalDate recurringEndDate;
    private List<Integer> daysOfWeek;
    private Integer durationMonths;
    private double totalBookingPrice;
    private double depositPrice;
    private List<SlotData> slots;
    private List<Long> temporaryBookingIds;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SlotData implements Serializable {
        private static final long serialVersionUID = 1L;
        private LocalDate date;
        private double price;
        private long sale;

        @JsonCreator
        public static SlotData of(@JsonProperty("date") LocalDate date,
                                  @JsonProperty("price") double price,
                                  @JsonProperty("sale") long sale) {
            return new SlotData(date, price, sale);
        }
    }
}
