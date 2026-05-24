-- Tạo bảng chat_messages nếu chưa tồn tại.
-- Lý do: một số database được baseline từ trước khi V1 chứa bảng này,
-- nên `chat_messages` có thể bị thiếu dù V1 đã được đánh dấu là đã chạy.
CREATE TABLE IF NOT EXISTS `chat_messages` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT,
  `match_post_id` BIGINT NOT NULL,
  `sender_id`     BIGINT NOT NULL,
  `content`       TEXT   NOT NULL,
  `sent_at`       DATETIME,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_cm_post`   FOREIGN KEY (`match_post_id`) REFERENCES `match_posts` (`id`),
  CONSTRAINT `fk_cm_sender` FOREIGN KEY (`sender_id`)     REFERENCES `user`        (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
