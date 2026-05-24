package com.example.quanly.domain;

public enum NotificationType {
    /** User huỷ booking → gửi cho admin/staff để biết phải hoàn cọc */
    REFUND_REQUEST,
    /** Admin xác nhận đã hoàn cọc → báo lại user */
    REFUND_DONE,
    /** User huỷ booking thành công → tin tự gửi vào hộp thư user */
    BOOKING_CANCELLED,
    /** Generic system notification */
    SYSTEM
}
