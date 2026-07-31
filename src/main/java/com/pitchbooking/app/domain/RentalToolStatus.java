package com.pitchbooking.app.domain;

import java.util.Locale;

public enum RentalToolStatus {
    PENDING("Chờ nhận phụ kiện"),
    RENTING("Đang thuê"),
    COMPLETED("Đã trả phụ kiện"),
    CANCELLED("Đã hủy");

    private final String label;

    RentalToolStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static RentalToolStatus fromLabel(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Trạng thái thuê không được để trống.");
        }

        String normalizedInput = input.trim();
        for (RentalToolStatus s : values()) {
            if (s.label.equalsIgnoreCase(normalizedInput)) {
                return s;
            }
        }

        try {
            return RentalToolStatus.valueOf(normalizedInput.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái thuê không hợp lệ: " + input);
        }
    }
}
