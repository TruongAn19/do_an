package com.example.quanly.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalItem {

    @NotNull(message = "racketId không được rỗng")
    @Positive(message = "racketId phải > 0")
    private Long racketId;

    @Min(value = 1, message = "quantity phải >= 1")
    private int quantity;
}
