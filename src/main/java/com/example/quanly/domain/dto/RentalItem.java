package com.example.quanly.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Một dòng vợt được thuê kèm theo khi đặt sân (bundled rental).
 */
@Data
public class RentalItem {

    @NotNull(message = "racketId không được để trống")
    @Positive(message = "racketId không hợp lệ")
    private Long racketId;

    @Positive(message = "Số lượng vợt phải lớn hơn 0")
    private int quantity;
}
