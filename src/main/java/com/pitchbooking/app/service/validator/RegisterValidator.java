package com.pitchbooking.app.service.validator;

import org.springframework.stereotype.Service;

import com.pitchbooking.app.domain.dto.RegisterDTO;
import com.pitchbooking.app.service.UserService;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Service
public class RegisterValidator implements ConstraintValidator<RegisterChecked, RegisterDTO> {
    private final UserService userService;

    public RegisterValidator (UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean isValid(RegisterDTO user, ConstraintValidatorContext context) {
        if (user == null) {
            return true;
        }

        boolean valid = true;

        // Check if password fields match
        if (!Objects.equals(user.getPassword(), user.getConfirmPassword())) {
            context.buildConstraintViolationWithTemplate("Passwords must match")
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            valid = false;
        }

        if (StringUtils.hasText(user.getEmail()) && this.userService.checkEmailExist(user.getEmail())) {
            context.buildConstraintViolationWithTemplate("email đã tồn tài")
                    .addPropertyNode("email")
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            valid = false;
        }
        // Additional validations can be added here

        return valid;
    }
}
