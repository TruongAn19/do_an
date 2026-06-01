package com.example.quanly.domain;

public enum NotificationType {
    REFUND_REQUEST,    // gửi cho admin/staff khi user huỷ booking
    REFUND_DONE,       // gửi cho user khi admin xác nhận đã hoàn cọc
    BOOKING_CANCELLED, // gửi cho user khi huỷ booking thành công
    SYSTEM             // thông báo hệ thống chung
}
