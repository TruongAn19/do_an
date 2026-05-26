package com.pitchbooking.app.domain.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates cross-field constraints on CreateRentalRequest:
 *  - DAILY  : rentalDate and quantityDay (>= 1) are required
 *  - ON_SITE: bookingCode is required
 */
@Documented
@Constraint(validatedBy = RentalContextValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRentalContext {
    String message() default "Thông tin thuê thiết bị không hợp lệ theo loại thuê";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
