package com.pitchbooking.app.domain;

public enum NotificationType {
    REFUND_REQUEST,    // staff/admin: user vừa huỷ, cần hoàn cọc
    REFUND_DONE,       // user: admin đã xác nhận hoàn cọc
    BOOKING_CANCELLED, // user: huỷ thành công
    SYSTEM
}
