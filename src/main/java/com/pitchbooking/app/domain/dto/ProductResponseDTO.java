package com.pitchbooking.app.domain.dto;

import com.pitchbooking.app.domain.PitchType;
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
    private String addressDetail;
    /**
     * Computed: price * (1 - sale/100) * 0.5.
     * Filled by ProductMapper — there is no longer a deposit_price column.
     */
    private double depositPrice;
    private String status;
    private String ownerName;
    private PitchType pitchType;
}
