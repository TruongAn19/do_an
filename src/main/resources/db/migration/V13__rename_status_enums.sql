-- Rename các giá trị enum lưu STRING trong DB sau khi đổi tên enum trong code:
--   RentalToolStatus.PAID  -> IN_USE  (label "Đang thuê" giữ nguyên)
--   RefundStatus.NONE      -> NOT_APPLICABLE (gộp lại — NONE bị xoá khỏi enum)

-- 1) rental_tool.status: PAID -> IN_USE
UPDATE `rental_tool` SET `status` = 'IN_USE' WHERE `status` = 'PAID';

-- 2) booking.refund_status: NONE -> NOT_APPLICABLE
UPDATE `booking` SET `refund_status` = 'NOT_APPLICABLE' WHERE `refund_status` = 'NONE';

-- 3) Đổi default của cột refund_status để raw INSERT (không qua JPA) cũng dùng giá trị mới.
ALTER TABLE `booking`
  MODIFY COLUMN `refund_status` VARCHAR(32) DEFAULT 'NOT_APPLICABLE';
