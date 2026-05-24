-- Thêm các cột phục vụ chức năng huỷ booking + hoàn cọc.
-- An toàn để chạy nhiều lần: kiểm tra cột tồn tại trước khi thêm.

ALTER TABLE `booking`
  ADD COLUMN `refund_status` VARCHAR(32) DEFAULT 'NONE',
  ADD COLUMN `refund_amount` DOUBLE,
  ADD COLUMN `cancelled_at` DATETIME,
  ADD COLUMN `used_sessions_at_cancel` INT,
  ADD COLUMN `total_sessions_at_cancel` INT,
  ADD COLUMN `cancel_reason` VARCHAR(255);

-- Backfill: các booking cũ → refund_status = 'NONE'
UPDATE `booking` SET `refund_status` = 'NONE' WHERE `refund_status` IS NULL;
