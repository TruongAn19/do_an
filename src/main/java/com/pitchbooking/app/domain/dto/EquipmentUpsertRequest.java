package com.pitchbooking.app.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class EquipmentUpsertRequest {

    @NotBlank(message = "Tên thiết bị không được để trống")
    private String name;

    private String factory;

    @NotNull(message = "Giá trị thiết bị không được để trống")
    @PositiveOrZero(message = "Giá trị thiết bị không được âm")
    private Double price;

    @NotNull(message = "Trạng thái khả dụng không được để trống")
    private Boolean available;

    @NotNull(message = "Giá thuê theo ngày không được để trống")
    @PositiveOrZero(message = "Giá thuê theo ngày không được âm")
    private Double rentalPricePerDay;

    @NotNull(message = "Giá thuê tại sân không được để trống")
    @PositiveOrZero(message = "Giá thuê tại sân không được âm")
    private Double rentalPricePerPlay;

    @NotNull(message = "Tồn kho tại sân không được để trống")
    @PositiveOrZero(message = "Tồn kho tại sân không được âm")
    private Integer bookingStockQuantity;

    @NotNull(message = "Tổng tồn kho không được để trống")
    @PositiveOrZero(message = "Tổng tồn kho không được âm")
    private Integer quantity;

    @NotBlank(message = "Trạng thái thiết bị không được để trống")
    private String status;

    @NotNull(message = "Sân quản lý thiết bị không được để trống")
    @Positive(message = "Sân quản lý thiết bị không hợp lệ")
    private Long productId;
}
