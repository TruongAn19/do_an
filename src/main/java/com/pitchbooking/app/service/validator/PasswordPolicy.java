package com.pitchbooking.app.service.validator;

import org.springframework.util.StringUtils;

public final class PasswordPolicy {

    public static final int MIN_LENGTH = 6;
    public static final String MIN_LENGTH_MESSAGE = "Mật khẩu phải có tối thiểu 6 ký tự";

    private PasswordPolicy() {
    }

    public static void validate(String password) {
        if (!StringUtils.hasText(password) || password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(MIN_LENGTH_MESSAGE);
        }
    }
}
