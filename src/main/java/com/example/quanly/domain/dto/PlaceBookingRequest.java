package com.example.quanly.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PlaceBookingRequest {

    @NotBlank(message = "Tên người nhận không được để trống")
    private String receiverName;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String receiverAddress;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^\\d{10,11}$", message = "Số điện thoại phải là 10-11 chữ số")
    private String receiverPhone;

    @Positive(message = "Sân không hợp lệ")
    private long productId;

    @Positive(message = "Khung giờ không hợp lệ")
    private long availableTimeId;

    @Positive(message = "Sân phụ không hợp lệ")
    private long courtId;

    @NotNull(message = "Ngày đặt không được để trống")
    @FutureOrPresent(message = "Ngày đặt không thể ở quá khứ")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bookingDate;

    @Pattern(regexp = "ONE_TIME|WEEKLY_RECURRING", message = "bookingType phải là ONE_TIME hoặc WEEKLY_RECURRING")
    private String bookingType = "ONE_TIME";

    @FutureOrPresent(message = "Ngày kết thúc chu kỳ không thể ở quá khứ")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recurringEndDate;

    /** Danh sách vợt thuê kèm (bundled rental). Nullable — chỉ áp dụng cho ONE_TIME. */
    @Valid
    private List<RentalItem> rackets;
}
