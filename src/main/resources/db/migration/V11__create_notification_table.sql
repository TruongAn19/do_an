-- Nhóm 2: Notification (lưu DB + push WS realtime)
CREATE TABLE `notification` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT,
  `recipient_user_id` BIGINT,
  `type`              VARCHAR(32),
  `ref_type`          VARCHAR(32),
  `ref_id`            BIGINT,
  `title`             VARCHAR(255),
  `message`           TEXT,
  `is_read`           BIT(1)       NOT NULL DEFAULT b'0',
  `created_at`        DATETIME,
  PRIMARY KEY (`id`),
  KEY `idx_notification_recipient_unread` (`recipient_user_id`, `is_read`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
