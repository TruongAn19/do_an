-- Nhóm 2: Cancel booking + refund tracking
ALTER TABLE `booking` ADD COLUMN `refund_status`            VARCHAR(32) DEFAULT 'NONE';
ALTER TABLE `booking` ADD COLUMN `refund_amount`            DOUBLE;
ALTER TABLE `booking` ADD COLUMN `cancelled_at`             DATETIME;
ALTER TABLE `booking` ADD COLUMN `used_sessions_at_cancel`  INT;
ALTER TABLE `booking` ADD COLUMN `total_sessions_at_cancel` INT;
ALTER TABLE `booking` ADD COLUMN `cancel_reason`            VARCHAR(255);
