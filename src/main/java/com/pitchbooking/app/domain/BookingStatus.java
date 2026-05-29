package com.pitchbooking.app.domain;

public enum BookingStatus {
    CHO_THANH_TOAN("Chờ thanh toán"),
    DA_DAT("Đã đặt cọc"),
    DA_THANH_TOAN("Đã thanh toán"),
    DA_HUY("Đã hủy");

    private final String label;

    BookingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static BookingStatus fromLabel(String input) {
        if (input == null || input.isEmpty()) return CHO_THANH_TOAN;

        // Try matching by label first
        for (BookingStatus s : values()) {
            if (s.label.equalsIgnoreCase(input)) return s;
        }

        // Legacy alias: trước đây DA_DAT có label "Đã đặt" — accept để tránh
        // crash convertToEntityAttribute nếu DB còn row chưa migrate.
        if ("Đã đặt".equalsIgnoreCase(input)) return DA_DAT;

        // Try matching by enum name
        try {
            return BookingStatus.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái booking không hợp lệ: " + input);
        }
    }
}
