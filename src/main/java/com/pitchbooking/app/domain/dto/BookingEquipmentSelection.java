package com.pitchbooking.app.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BookingEquipmentSelection {
    @NotNull(message = "Thiếu mã phụ kiện")
    @Positive(message = "Mã phụ kiện không hợp lệ")
    private Long equipmentId;

    @Positive(message = "Số lượng phụ kiện phải lớn hơn 0")
    private int quantity;
}
