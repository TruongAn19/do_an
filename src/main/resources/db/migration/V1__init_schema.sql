-- ============================================================
-- V1__init_schema.sql  --  Baseline schema (Football Pitch Management)
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
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `email`        VARCHAR(255) NOT NULL,
  `password`     VARCHAR(255) NOT NULL,
  `full_name`    VARCHAR(255) NOT NULL,
  `address`      VARCHAR(500),
  `phone`        VARCHAR(20)  NOT NULL,
  `avatar`       VARCHAR(500),
  `member_level` VARCHAR(20)  NOT NULL DEFAULT 'NORMAL',
  `role_id`      BIGINT,
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
CREATE TABLE `products` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT,
  `name`         VARCHAR(255),
  `price`        DOUBLE,
  `image`        VARCHAR(500),
  `detail_desc`  MEDIUMTEXT,
  `short_desc`   VARCHAR(500),
  `quantity`     BIGINT,
  `sale`         BIGINT,
  `address`      VARCHAR(500),
  `deposit_price` DOUBLE,
  `status`       VARCHAR(50),
  `user_id`      BIGINT,
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
  `id`                BIGINT       NOT NULL AUTO_INCREMENT,
  `total_price`       DOUBLE,
  `receiver_name`     VARCHAR(255),
  `booking_code`      VARCHAR(255),
  `receiver_address`  VARCHAR(500),
  `receiver_phone`    VARCHAR(20),
  `status`            VARCHAR(100),
  `booking_date`      DATE,
  `deposit_price`     DOUBLE,
  `user_id`           BIGINT,
  `available_time_id` BIGINT,
  `rental_tool_code`  VARCHAR(255) DEFAULT 'KHONG_THUE',
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_booking_user` FOREIGN KEY (`user_id`)           REFERENCES `user`          (`id`),
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
  CONSTRAINT `fk_bd_booking`  FOREIGN KEY (`booking_id`)        REFERENCES `booking`       (`id`),
  CONSTRAINT `fk_bd_product`  FOREIGN KEY (`product_id`)        REFERENCES `products`      (`id`),
  CONSTRAINT `fk_bd_time`     FOREIGN KEY (`available_time_id`) REFERENCES `available_time` (`id`),
  CONSTRAINT `fk_bd_subpitch` FOREIGN KEY (`sub_pitch_id`)      REFERENCES `sub_pitches`   (`id`)
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
  `user_id`          BIGINT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 13. temporary_booking
CREATE TABLE `temporary_booking` (
  `id`                BIGINT NOT NULL AUTO_INCREMENT,
  `user_id`           BIGINT,
  `sub_pitch_id`      BIGINT,
  `available_time_id` BIGINT,
  `booking_date`      DATE,
  `hold_start_time`   DATETIME,
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
