-- Thêm cột phục vụ huỷ + hoàn cọc cho đơn thuê vợt standalone (DAILY).
-- Mirror V10 (booking). RefundStatus lưu dạng STRING: NOT_APPLICABLE / PENDING_REFUND / REFUNDED.

ALTER TABLE `rental_tool`
  ADD COLUMN `refund_status` VARCHAR(32) DEFAULT 'NOT_APPLICABLE',
  ADD COLUMN `cancelled_at` DATETIME;

-- Backfill: đơn cũ → NOT_APPLICABLE
UPDATE `rental_tool` SET `refund_status` = 'NOT_APPLICABLE' WHERE `refund_status` IS NULL;
