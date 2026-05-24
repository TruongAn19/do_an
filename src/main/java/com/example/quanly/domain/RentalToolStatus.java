package com.example.quanly.domain;

public enum RentalToolStatus {
    PENDING("Chờ bàn giao"),
    IN_USE("Đang thuê"),
    COMPLETED("Đã trả"),
    CANCELLED("Hủy bỏ");

    private final String label;

    RentalToolStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Parse từ tên enum (PENDING/IN_USE/...) HOẶC label tiếng Việt (Chờ bàn giao/Đã trả/...).
     * Cho phép FE gửi label hiển thị mà không cần map riêng.
     */
    public static RentalToolStatus from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Trạng thái không được rỗng.");
        }
        for (RentalToolStatus s : values()) {
            if (s.name().equalsIgnoreCase(value) || s.label.equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Trạng thái không hợp lệ: " + value);
    }
}
