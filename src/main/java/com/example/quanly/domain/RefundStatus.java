package com.example.quanly.domain;

public enum RefundStatus {
    NOT_APPLICABLE("Không áp dụng"),
    PENDING_REFUND("Chờ hoàn cọc"),
    REFUNDED("Đã hoàn cọc");

    private final String label;

    RefundStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
