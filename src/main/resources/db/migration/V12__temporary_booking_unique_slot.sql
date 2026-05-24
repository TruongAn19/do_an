-- Đảm bảo không tồn tại hai TemporaryBooking trùng (sub_court_id, available_time_id, booking_date).
-- Cần thiết cho fix race condition WEEKLY_RECURRING: BookingService giờ tạo hold cho mọi
-- ngày trong chu kỳ, hai user đặt cùng slot song song sẽ bị chặn ở DB.

-- Dọn duplicate cũ trước khi thêm constraint (giữ lại bản ghi có hold_start_time mới nhất).
DELETE tb1 FROM temporary_booking tb1
INNER JOIN temporary_booking tb2
  ON tb1.sub_court_id = tb2.sub_court_id
 AND tb1.available_time_id = tb2.available_time_id
 AND tb1.booking_date = tb2.booking_date
 AND (
       tb1.hold_start_time < tb2.hold_start_time
       OR (tb1.hold_start_time = tb2.hold_start_time AND tb1.id < tb2.id)
     );

ALTER TABLE temporary_booking
  ADD CONSTRAINT uk_temp_booking_slot
  UNIQUE (sub_court_id, available_time_id, booking_date);
