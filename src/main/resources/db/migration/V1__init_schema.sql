-- ============================================================
-- V1__init_schema.sql  --  Baseline schema (derived from Java entities)
-- ============================================================

-- 1. roles  (Role.java → @Table("roles"))
CREATE TABLE `roles` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(255),
  `description` TEXT,
  PRIMARY KEY (`id`)
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

-- 8. sub_courts  (SubCourt.java → @Table("sub_courts"))
CREATE TABLE `sub_courts` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `name`       VARCHAR(255),
  `product_id` BIGINT,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_subcourt_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 9. subcourt_available_time  (SubCourtAvailableTime.java → @Table("subcourt_available_time"))
CREATE TABLE `subcourt_available_time` (
  `id`                BIGINT NOT NULL AUTO_INCREMENT,
  `sub_court_id`      BIGINT,
  `available_time_id` BIGINT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sat` (`sub_court_id`, `available_time_id`),
  CONSTRAINT `fk_sat_subcourt` FOREIGN KEY (`sub_court_id`)      REFERENCES `sub_courts`    (`id`),
  CONSTRAINT `fk_sat_time`     FOREIGN KEY (`available_time_id`) REFERENCES `available_time` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 10. booking  (Booking.java → @Table("booking"))
--     booking_type & recurring_end_date are added by V3
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
  `user_id`          BIGINT,
  `available_time_id` BIGINT,
  `rental_tool_code` VARCHAR(255) DEFAULT 'KHONG_THUE',
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_booking_user` FOREIGN KEY (`user_id`)           REFERENCES `user`          (`id`),
  CONSTRAINT `fk_booking_time` FOREIGN KEY (`available_time_id`) REFERENCES `available_time` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 11. booking_detail  (BookingDetail.java → @Table("booking_detail"))
CREATE TABLE `booking_detail` (
  `id`                BIGINT NOT NULL AUTO_INCREMENT,
  `price`             DOUBLE,
  `sale`              BIGINT,
  `booking_id`        BIGINT,
  `product_id`        BIGINT,
  `available_time_id` BIGINT,
  `sub_court_id`      BIGINT,
  `date`              DATE,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_bd_booking`  FOREIGN KEY (`booking_id`)        REFERENCES `booking`       (`id`),
  CONSTRAINT `fk_bd_product`  FOREIGN KEY (`product_id`)        REFERENCES `products`      (`id`),
  CONSTRAINT `fk_bd_time`     FOREIGN KEY (`available_time_id`) REFERENCES `available_time` (`id`),
  CONSTRAINT `fk_bd_subcourt` FOREIGN KEY (`sub_court_id`)      REFERENCES `sub_courts`    (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 12. match_posts  (MatchPost.java → @Table("match_posts"))
CREATE TABLE `match_posts` (
  `id`                  BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`             BIGINT       NOT NULL,
  `play_date`           DATE         NOT NULL,
  `area`                VARCHAR(255) NOT NULL,
  `time_slot`           VARCHAR(255) NOT NULL,
  `skill_level`         VARCHAR(255),
  `description`         TEXT,
  `status`              VARCHAR(50)  DEFAULT 'open',
  `created_at`          DATETIME,
  `max_participants`    INT,
  `current_participants` INT,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_mp_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 13. match_participants  (MatchParticipant.java → @Table("match_participants"))
CREATE TABLE `match_participants` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT,
  `match_post_id` BIGINT NOT NULL,
  `user_id`       BIGINT NOT NULL,
  `joined_at`     DATETIME,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_mpart_post` FOREIGN KEY (`match_post_id`) REFERENCES `match_posts` (`id`),
  CONSTRAINT `fk_mpart_user` FOREIGN KEY (`user_id`)       REFERENCES `user`        (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 14. chat_messages  (ChatMessage.java → @Table("chat_messages"))
CREATE TABLE `chat_messages` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT,
  `match_post_id` BIGINT NOT NULL,
  `sender_id`     BIGINT NOT NULL,
  `content`       TEXT   NOT NULL,
  `sent_at`       DATETIME,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_cm_post`   FOREIGN KEY (`match_post_id`) REFERENCES `match_posts` (`id`),
  CONSTRAINT `fk_cm_sender` FOREIGN KEY (`sender_id`)     REFERENCES `user`        (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 15. racket_stock_by_date  (RacketStockByDate.java → default table name)
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

-- 16. rental_tool  (RentalTool.java → default table name)
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
  `create_at`        DATETIME,
  `update_at`        DATETIME,
  `rental_tool_code` VARCHAR(255),
  `user_id`          BIGINT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 17. temporary_booking  (TemporaryBooking.java → @Table("temporary_booking"))
CREATE TABLE `temporary_booking` (
  `id`                BIGINT NOT NULL AUTO_INCREMENT,
  `user_id`           BIGINT,
  `sub_court_id`      BIGINT,
  `available_time_id` BIGINT,
  `booking_date`      DATE,
  `hold_start_time`   DATETIME,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_tb_subcourt` FOREIGN KEY (`sub_court_id`)      REFERENCES `sub_courts`    (`id`),
  CONSTRAINT `fk_tb_time`     FOREIGN KEY (`available_time_id`) REFERENCES `available_time` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 18. password_reset_token  (PasswordResetToken.java → default table name)
CREATE TABLE `password_reset_token` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `token`       VARCHAR(255),
  `expiry_date` DATETIME,
  `user_id`     BIGINT,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_prt_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
