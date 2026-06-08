-- A1: chống double-booking ở booking_detail.
-- booking_detail không có cột status (cờ huỷ nằm ở bảng booking),
-- nên dùng cột active_slot_key do app quản lý thay cho generated column:
--   - booking ACTIVE  -> key = concat(sub_court_id-available_time_id-date)
--   - booking ĐÃ HUỶ  -> key = NULL  (NULL được phép trùng -> cho đặt lại slot)
-- Lưu ý: booking.status lưu dạng LABEL tiếng Việt qua BookingStatusConverter,
-- nên so sánh với 'Đã hủy' (không phải 'DA_HUY').

ALTER TABLE booking_detail ADD COLUMN active_slot_key VARCHAR(64) NULL;

-- Backfill cho dữ liệu cũ. Với mỗi nhóm (sub_court_id, available_time_id, date)
-- thuộc booking ACTIVE, CHỈ giữ key cho bản ghi id nhỏ nhất (dọn trùng cũ nếu có);
-- các bản còn lại để NULL để không vi phạm UNIQUE sắp thêm.
UPDATE booking_detail bd
JOIN booking b ON bd.booking_id = b.id
SET bd.active_slot_key = CONCAT(bd.sub_court_id, '-', bd.available_time_id, '-', bd.date)
WHERE b.status <> 'Đã hủy'
  AND bd.id = (
    SELECT t.min_id FROM (
      SELECT MIN(bd2.id) AS min_id
      FROM booking_detail bd2
      JOIN booking b2 ON bd2.booking_id = b2.id
      WHERE b2.status <> 'Đã hủy'
        AND bd2.sub_court_id      = bd.sub_court_id
        AND bd2.available_time_id = bd.available_time_id
        AND bd2.date              = bd.date
    ) AS t
  );

ALTER TABLE booking_detail
  ADD CONSTRAINT uk_booking_detail_active_slot UNIQUE (active_slot_key);
