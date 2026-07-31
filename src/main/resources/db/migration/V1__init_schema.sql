-- ============================================================
-- V1__init_schema.sql  --  Baseline schema (derived from Java entities)
-- ============================================================

-- 1. roles  (Role.java → @Table("roles"))
CREATE TABLE `roles` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(255) NOT NULL,
  `description` TEXT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_roles_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2. user  (User.java → @Table("user"))
CREATE TABLE `user` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `email`        VARCHAR(255) NOT NULL,
  `password`     VARCHAR(255) NOT NULL,
  `full_name`    VARCHAR(255) NOT NULL,
  `address`      VARCHAR(500),
  `phone`        VARCHAR(20)  NOT NULL,
  `avatar`       VARCHAR(500),
  `member_level` VARCHAR(20)  NOT NULL DEFAULT 'NORMAL',
  `active`       BIT(1)       NOT NULL DEFAULT b'1',
  `role_id`      BIGINT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_email` (`email`),
  CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3. available_time  (AvailableTime.java → default table name)
CREATE TABLE `available_time` (
  `id`   BIGINT NOT NULL AUTO_INCREMENT,
  `time` TIME,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 4. products  (Product.java → @Table("products"))
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
  `address_detail` VARCHAR(255),
  `deposit_price` DOUBLE,
  `status`       VARCHAR(50),
  `user_id`      BIGINT,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_product_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 5. court_time  (explicit @JoinTable in Product.availableTimes)
CREATE TABLE `court_time` (
  `court_id` BIGINT NOT NULL,
  `time_id`  BIGINT NOT NULL,
  PRIMARY KEY (`court_id`, `time_id`),
  CONSTRAINT `fk_ct_court` FOREIGN KEY (`court_id`) REFERENCES `products`      (`id`),
  CONSTRAINT `fk_ct_time`  FOREIGN KEY (`time_id`)  REFERENCES `available_time` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 6. racket  (Racket.java → @Table("racket"))
CREATE TABLE `racket` (
  `id`                    BIGINT       NOT NULL AUTO_INCREMENT,
  `name`                  VARCHAR(100) NOT NULL,
  `price`                 DOUBLE,
  `available`             BIT(1)       NOT NULL DEFAULT b'1',
  `factory`               VARCHAR(255),
  `image`                 VARCHAR(500),
  `rental_price_per_day`  DOUBLE,
  `rental_price_per_play` DOUBLE,
  `booking_stock_quantity` INT,
  `quantity`              INT,
  `status`                VARCHAR(50),
  `product_id`            BIGINT,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_racket_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 7. sub_courts  (SubCourt.java -> @Table("sub_courts"))
CREATE TABLE `sub_courts` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `name`       VARCHAR(255),
  `product_id` BIGINT,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_subcourt_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 8. subcourt_available_time  (SubCourtAvailableTime.java -> @Table("subcourt_available_time"))
CREATE TABLE `subcourt_available_time` (
  `id`                BIGINT NOT NULL AUTO_INCREMENT,
  `sub_court_id`      BIGINT,
  `available_time_id` BIGINT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sat` (`sub_court_id`, `available_time_id`),
  CONSTRAINT `fk_sat_subcourt` FOREIGN KEY (`sub_court_id`)      REFERENCES `sub_courts`    (`id`),
  CONSTRAINT `fk_sat_time`     FOREIGN KEY (`available_time_id`) REFERENCES `available_time` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 9. booking  (Booking.java -> @Table("booking"))
CREATE TABLE `booking` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT,
  `total_price`      DOUBLE,
  `receiver_name`    VARCHAR(255),
  `booking_code`     VARCHAR(255),
  `receiver_address` VARCHAR(500),
  `receiver_phone`   VARCHAR(20),
  `status`           VARCHAR(100),
  `booking_date`     DATE,
  `deposit_price`    DOUBLE,
  `booking_type`     VARCHAR(20) DEFAULT 'ONE_TIME',
  `recurring_end_date` DATE,
  `user_id`          BIGINT,
  `available_time_id` BIGINT,
  `rental_tool_code` VARCHAR(255) DEFAULT 'KHONG_THUE',
  `refund_status`    VARCHAR(32) DEFAULT 'NONE',
  `refund_amount`    DOUBLE,
  `cancelled_at`     DATETIME,
  `used_sessions_at_cancel` INT,
  `total_sessions_at_cancel` INT,
  `cancel_reason`    VARCHAR(255),
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_booking_user` FOREIGN KEY (`user_id`)           REFERENCES `user`          (`id`),
  CONSTRAINT `fk_booking_time` FOREIGN KEY (`available_time_id`) REFERENCES `available_time` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 10. booking_detail  (BookingDetail.java -> @Table("booking_detail"))
CREATE TABLE `booking_detail` (
  `id`                BIGINT NOT NULL AUTO_INCREMENT,
  `price`             DOUBLE,
  `sale`              BIGINT,
  `booking_id`        BIGINT,
  `product_id`        BIGINT,
  `available_time_id` BIGINT,
  `sub_court_id`      BIGINT,
  `date`              DATE,
  `slot_active`       BIT(1) NULL DEFAULT b'1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_active_booking_slot`
    (`sub_court_id`, `available_time_id`, `date`, `slot_active`),
  CONSTRAINT `fk_bd_booking`  FOREIGN KEY (`booking_id`)        REFERENCES `booking`       (`id`),
  CONSTRAINT `fk_bd_product`  FOREIGN KEY (`product_id`)        REFERENCES `products`      (`id`),
  CONSTRAINT `fk_bd_time`     FOREIGN KEY (`available_time_id`) REFERENCES `available_time` (`id`),
  CONSTRAINT `fk_bd_subcourt` FOREIGN KEY (`sub_court_id`)      REFERENCES `sub_courts`    (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 11. racket_stock_by_date
CREATE TABLE `racket_stock_by_date` (
  `id`              BIGINT NOT NULL AUTO_INCREMENT,
  `racket_id`       BIGINT,
  `date`            DATE,
  `available_stock` INT,
  `reserved_stock`  INT,
  `rental_stock`    INT,
  `total_stock`     INT,
  `created_at`      DATETIME,
  `updated_at`      DATETIME,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_racket_date` (`racket_id`, `date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 12. rental_tool
CREATE TABLE `rental_tool` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT,
  `full_name`        VARCHAR(255),
  `email`            VARCHAR(255),
  `phone`            VARCHAR(20),
  `type`             VARCHAR(20),
  `booking_id`       VARCHAR(255),
  `racket_id`        BIGINT,
  `product_id`       BIGINT,
  `price`            DOUBLE,
  `rental_price`     DOUBLE,
  `status`           VARCHAR(20),
  `quantity`         INT,
  `quantity_day`     INT,
  `rental_date`      DATE,
  `return_date`      DATE,
  `last_stock_activated_date` DATE,
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
  `sub_court_id`      BIGINT,
  `available_time_id` BIGINT,
  `booking_date`      DATE,
  `hold_start_time`   DATETIME,
  `expires_at`        DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_temporary_booking_slot`
    (`sub_court_id`, `available_time_id`, `booking_date`),
  KEY `idx_temporary_booking_expiry` (`expires_at`),
  CONSTRAINT `fk_tb_subcourt` FOREIGN KEY (`sub_court_id`)      REFERENCES `sub_courts`    (`id`),
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
  `recipient_user_id` BIGINT,
  `type`              VARCHAR(32),
  `ref_type`          VARCHAR(32),
  `ref_id`            BIGINT,
  `title`             VARCHAR(255),
  `message`           TEXT,
  `is_read`           BIT(1)       NOT NULL DEFAULT b'0',
  `created_at`        DATETIME,
  PRIMARY KEY (`id`),
  KEY `idx_notification_recipient_unread`
    (`recipient_user_id`, `is_read`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 16. pending_booking_payment
CREATE TABLE `pending_booking_payment` (
  `id`                         BIGINT NOT NULL AUTO_INCREMENT,
  `temporary_booking_id`       BIGINT,
  `temporary_booking_ids_json` LONGTEXT,
  `user_id`                    BIGINT NOT NULL,
  `product_id`                 BIGINT NOT NULL,
  `available_time_id`          BIGINT NOT NULL,
  `sub_court_id`               BIGINT NOT NULL,
  `receiver_name`              VARCHAR(255),
  `receiver_address`           VARCHAR(500),
  `receiver_phone`             VARCHAR(20),
  `first_booking_date`         DATE NOT NULL,
  `booking_type`               VARCHAR(30) NOT NULL,
  `recurring_end_date`         DATE,
  `total_booking_price`        DOUBLE NOT NULL,
  `deposit_price`              DOUBLE NOT NULL,
  `slots_json`                 LONGTEXT NOT NULL,
  `rental_slots_json`          LONGTEXT,
  `expires_at`                 DATETIME NOT NULL,
  `processing_status`          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `completed_booking_id`       BIGINT,
  `completed_booking_code`     VARCHAR(255),
  PRIMARY KEY (`id`),
  KEY `idx_pending_booking_expiry` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
