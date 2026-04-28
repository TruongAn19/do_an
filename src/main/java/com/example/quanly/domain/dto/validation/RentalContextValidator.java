package com.example.quanly.domain.dto.validation;

import com.example.quanly.domain.RentalType;
import com.example.quanly.domain.dto.CreateRentalRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RentalContextValidator implements ConstraintValidator<ValidRentalContext, CreateRentalRequest> {

    @Override
    public boolean isValid(CreateRentalRequest req, ConstraintValidatorContext ctx) {
        if (req.getType() == null) return true; // @NotNull trên field đã xử lý

        ctx.disableDefaultConstraintViolation();
        boolean valid = true;

        if (req.getType() == RentalType.DAILY) {
            if (req.getRentalDate() == null) {
                ctx.buildConstraintViolationWithTemplate("Ngày thuê không được để trống với loại DAILY")
                        .addPropertyNode("rentalDate").addConstraintViolation();
                valid = false;
            }
            if (req.getQuantityDay() < 1) {
                ctx.buildConstraintViolationWithTemplate("Số ngày thuê phải ít nhất là 1 với loại DAILY")
                        .addPropertyNode("quantityDay").addConstraintViolation();
                valid = false;
            }
        } else if (req.getType() == RentalType.ON_SITE) {
            if (req.getBookingCode() == null || req.getBookingCode().isBlank()) {
                ctx.buildConstraintViolationWithTemplate("Mã booking không được để trống với loại ON_SITE")
                        .addPropertyNode("bookingCode").addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }
}
