package com.pitchbooking.app.domain.dto;

import com.pitchbooking.app.domain.PitchType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ProductUpsertRequest {

    @NotBlank(message = "Tên sân không được để trống")
    private String name;

    @NotNull(message = "Giá sân không được để trống")
    @PositiveOrZero(message = "Giá sân không được âm")
    private Double price;

    private String image;

    @NotBlank(message = "Mô tả sân không được để trống")
    private String detailDesc;

    @NotBlank(message = "Mô tả ngắn không được để trống")
    private String shortDesc;

    @NotNull(message = "Số lượng sân con không được để trống")
    @Positive(message = "Số lượng sân con phải lớn hơn 0")
    private Long quantity;

    @NotNull(message = "Khuyến mãi không được để trống")
    @Min(value = 0, message = "Khuyến mãi không được âm")
    @Max(value = 100, message = "Khuyến mãi không được vượt quá 100%")
    private Long sale;

    @NotBlank(message = "Địa chỉ sân không được để trống")
    private String address;

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    private String addressDetail;

    private String status;

    @NotNull(message = "Loại sân không được để trống")
    private PitchType pitchType;

    private String subPitchNames;
}
