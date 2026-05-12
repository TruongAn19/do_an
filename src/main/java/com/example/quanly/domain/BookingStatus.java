package com.example.quanly.domain;

public enum BookingStatus {
    CHO_THANH_TOAN("Chờ thanh toán"),
    DA_DAT("Đã đặt"),
    DA_DAT_COC("Đã đặt cọc"),
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
        for (BookingStatus s : values()) {
            if (s.label.equals(input) || s.name().equalsIgnoreCase(input)) return s;
        }
        throw new IllegalArgumentException("Trạng thái booking không hợp lệ: " + input);
    }
}
