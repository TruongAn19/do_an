package com.pitchbooking.app.domain;

public enum RefundStatus {
    NONE("Không áp dụng"),
    PENDING_REFUND("Chờ hoàn cọc"),
    REFUNDED("Đã hoàn cọc"),
    NOT_APPLICABLE("Không hoàn cọc");

    private final String label;

    RefundStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
