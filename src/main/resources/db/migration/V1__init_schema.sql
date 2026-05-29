-- ============================================================
-- V1__init_schema.sql  --  Consolidated schema (Football Pitch Management)
--
-- Lịch sử migration đã được gộp vào file này. Các thay đổi từ V2-V15
-- (trừ V4/V5 seed data) đã được merge thẳng vào CREATE TABLE bên dưới:
--   V2  : user.referral_code, referrer_code
--   V3  : booking.booking_type, recurring_end_date
--   V6  : products.address_detail
--   V9  : booking.days_of_week, duration_months
--   V10 : sub_pitches.pitch_type
--   V11 : products.deposit_price đã bị loại khỏi schema (auto-computed)
--   V12 : booking.refund_status / refund_amount / cancelled_at / *_at_cancel / cancel_reason
--   V13 : notification table
--   V14 : temporary_booking.hold_expires_at (đổi tên từ hold_start_time)
--   V15 : data fix cho booking.status label — không cần ở DB mới
--   V8  : data fix address — không cần ở DB mới
-- Ngoài ra thêm cột refund cho rental_tool: refund_status, deposit_amount, cancelled_at
-- ============================================================

-- 1. roles
CREATE TABLE `roles` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(255),
  `description` TEXT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2. user
CREATE TABLE `user` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `email`         VARCHAR(255) NOT NULL,
  `password`      VARCHAR(255) NOT NULL,
  `full_name`     VARCHAR(255) NOT NULL,
  `address`       VARCHAR(500),
  `phone`         VARCHAR(20)  NOT NULL,
  `avatar`        VARCHAR(500),
  `member_level`  VARCHAR(20)  NOT NULL DEFAULT 'NORMAL',
  `referral_code` VARCHAR(20)  DEFAULT NULL,
  `referrer_code` VARCHAR(20)  DEFAULT NULL,
  `role_id`       BIGINT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_email` (`email`),
  CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3. available_time
CREATE TABLE `available_time` (
  `id`   BIGINT NOT NULL AUTO_INCREMENT,
  `time` TIME,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 4. products  (cụm sân bóng đá)
-- deposit_price đã bị loại bỏ — auto-computed = price * (1 - sale/100) * 0.5
CREATE TABLE `products` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT,
  `name`           VARCHAR(255),
  `price`          DOUBLE,
  `image`          VARCHAR(500),
  `detail_desc`    MEDIUMTEXT,
  `short_desc`     VARCHAR(500),
  `quantity`       BIGINT,
  `sale`           BIGINT,
  `address`        VARCHAR(500),
  `address_detail` VARCHAR(255),
  `status`         VARCHAR(50),
  `user_id`        BIGINT,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_product_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 5. pitch_time  (M2M Product ↔ AvailableTime — khung giờ hoạt động của cụm sân)
CREATE TABLE `pitch_time` (
  `product_id` BIGINT NOT NULL,
  `time_id`    BIGINT NOT NULL,
  PRIMARY KEY (`product_id`, `time_id`),
  CONSTRAINT `fk_pt_product` FOREIGN KEY (`product_id`) REFERENCES `products`       (`id`),
  CONSTRAINT `fk_pt_time`    FOREIGN KEY (`time_id`)    REFERENCES `available_time` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 6. equipment  (thiết bị cho thuê: bóng, giày, áo bib...)
CREATE TABLE `equipment` (
  `id`                     BIGINT       NOT NULL AUTO_INCREMENT,
  `name`                   VARCHAR(100) NOT NULL,
  `price`                  DOUBLE,
  `available`              BIT(1)       NOT NULL DEFAULT b'1',
  `factory`                VARCHAR(255),
  `image`                  VARCHAR(500),
  `rental_price_per_day`   DOUBLE,
  `rental_price_per_play`  DOUBLE,
  `booking_stock_quantity` INT,
  `quantity`               INT,
  `status`                 VARCHAR(50),
  `product_id`             BIGINT,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_equipment_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 7. sub_pitches  (sân con trong cụm sân)
CREATE TABLE `sub_pitches` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `name`       VARCHAR(255),
  `pitch_type` VARCHAR(20)  DEFAULT 'FIVE_ASIDE',
  `product_id` BIGINT,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_subpitch_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 8. subpitch_available_time  (M2M sân con ↔ khung giờ)
CREATE TABLE `subpitch_available_time` (
  `id`                BIGINT NOT NULL AUTO_INCREMENT,
  `sub_pitch_id`      BIGINT,
  `available_time_id` BIGINT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spat` (`sub_pitch_id`, `available_time_id`),
  CONSTRAINT `fk_spat_subpitch` FOREIGN KEY (`sub_pitch_id`)      REFERENCES `sub_pitches`    (`id`),
  CONSTRAINT `fk_spat_time`     FOREIGN KEY (`available_time_id`) REFERENCES `available_time` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 9. booking
CREATE TABLE `booking` (
  `id`                       BIGINT       NOT NULL AUTO_INCREMENT,
  `total_price`              DOUBLE,
  `receiver_name`            VARCHAR(255),
  `booking_code`             VARCHAR(255),
  `receiver_address`         VARCHAR(500),
  `receiver_phone`           VARCHAR(20),
  `status`                   VARCHAR(100),
  `booking_date`             DATE,
  `deposit_price`            DOUBLE,
  `user_id`                  BIGINT,
  `available_time_id`        BIGINT,
  `rental_tool_code`         VARCHAR(255) DEFAULT 'KHONG_THUE',
  `booking_type`             VARCHAR(20)  DEFAULT 'ONE_TIME',
  `recurring_end_date`       DATE,
  `days_of_week`             VARCHAR(255) NULL,
  `duration_months`          INT          NULL,
  `refund_status`            VARCHAR(32)  DEFAULT 'NONE',
  `refund_amount`            DOUBLE       NULL,
  `cancelled_at`             DATETIME     NULL,
  `used_sessions_at_cancel`  INT          NULL,
  `total_sessions_at_cancel` INT          NULL,
  `cancel_reason`            VARCHAR(255) NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_booking_user` FOREIGN KEY (`user_id`)           REFERENCES `user`           (`id`),
  CONSTRAINT `fk_booking_time` FOREIGN KEY (`available_time_id`) REFERENCES `available_time` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 10. booking_detail
CREATE TABLE `booking_detail` (
  `id`                BIGINT NOT NULL AUTO_INCREMENT,
  `price`             DOUBLE,
  `sale`              BIGINT,
  `booking_id`        BIGINT,
  `product_id`        BIGINT,
  `available_time_id` BIGINT,
  `sub_pitch_id`      BIGINT,
  `date`              DATE,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_bd_booking`  FOREIGN KEY (`booking_id`)        REFERENCES `booking`        (`id`),
  CONSTRAINT `fk_bd_product`  FOREIGN KEY (`product_id`)        REFERENCES `products`       (`id`),
  CONSTRAINT `fk_bd_time`     FOREIGN KEY (`available_time_id`) REFERENCES `available_time` (`id`),
  CONSTRAINT `fk_bd_subpitch` FOREIGN KEY (`sub_pitch_id`)      REFERENCES `sub_pitches`    (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 11. equipment_stock_by_date
CREATE TABLE `equipment_stock_by_date` (
  `id`              BIGINT NOT NULL AUTO_INCREMENT,
  `equipment_id`    BIGINT,
  `date`            DATE,
  `available_stock` INT,
  `reserved_stock`  INT,
  `rental_stock`    INT,
  `total_stock`     INT,
  `created_at`      DATETIME,
  `updated_at`      DATETIME,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_equipment_date` (`equipment_id`, `date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 12. rental_tool
-- refund_status / deposit_amount / cancelled_at được thêm cho luồng hoàn cọc thuê thiết bị.
CREATE TABLE `rental_tool` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT,
  `full_name`        VARCHAR(255),
  `email`            VARCHAR(255),
  `phone`            VARCHAR(20),
  `type`             VARCHAR(20),
  `booking_id`       VARCHAR(255),
  `equipment_id`     BIGINT,
  `product_id`       BIGINT,
  `price`            DOUBLE,
  `rental_price`     DOUBLE,
  `status`           VARCHAR(20),
  `quantity`         INT,
  `quantity_day`     INT,
  `rental_date`      DATE,
  `return_date`      DATE,
  `create_at`        DATETIME,
  `update_at`        DATETIME,
  `rental_tool_code` VARCHAR(255),
  `refund_status`    VARCHAR(32)  DEFAULT 'NONE',
  `deposit_amount`   DOUBLE       DEFAULT 0,
  `cancelled_at`     DATETIME     NULL,
  `user_id`          BIGINT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 13. temporary_booking
-- hold_expires_at lưu thời điểm hold hết hạn (đã đổi từ hold_start_time).
CREATE TABLE `temporary_booking` (
  `id`                BIGINT NOT NULL AUTO_INCREMENT,
  `user_id`           BIGINT,
  `sub_pitch_id`      BIGINT,
  `available_time_id` BIGINT,
  `booking_date`      DATE,
  `hold_expires_at`   DATETIME,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_tb_subpitch` FOREIGN KEY (`sub_pitch_id`)      REFERENCES `sub_pitches`    (`id`),
  CONSTRAINT `fk_tb_time`     FOREIGN KEY (`available_time_id`) REFERENCES `available_time` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 14. password_reset_token
CREATE TABLE `password_reset_token` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `token`       VARCHAR(255),
  `expiry_date` DATETIME,
  `user_id`     BIGINT,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_prt_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 15. notification
CREATE TABLE `notification` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT,
  `recipient_user_id` BIGINT       NOT NULL,
  `type`              VARCHAR(32)  NOT NULL,
  `ref_type`          VARCHAR(32),
  `ref_id`            BIGINT,
  `title`             VARCHAR(255),
  `message`           TEXT,
  `is_read`           BOOLEAN      NOT NULL DEFAULT FALSE,
  `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_notif_recipient_unread_created` (`recipient_user_id`, `is_read`, `created_at`),
  CONSTRAINT `fk_notif_recipient` FOREIGN KEY (`recipient_user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
