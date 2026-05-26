package com.pitchbooking.app.domain;

public enum RentalToolStatus {
    PENDING("Chờ thanh toán"),
    PAID("Đã thanh toán"),
    COMPLETED("Đã trả"),
    CANCELLED("Đã hủy");

    private final String label;

    RentalToolStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static RentalToolStatus fromLabel(String input) {
        if (input == null || input.isEmpty()) return PENDING;
        
        // Try matching by label first
        for (RentalToolStatus s : values()) {
            if (s.label.equalsIgnoreCase(input)) return s;
        }
        
        // Try matching by enum name
        try {
            return RentalToolStatus.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái thuê không hợp lệ: " + input);
        }
    }
}
