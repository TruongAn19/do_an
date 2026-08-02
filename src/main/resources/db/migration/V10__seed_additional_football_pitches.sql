-- ============================================================
-- V10__seed_additional_football_pitches.sql
-- Bổ sung 10 cụm sân bóng cùng sân con và khung giờ hoạt động.
--
-- Quy ước dữ liệu:
--   products.quantity                 = số sân con của cụm sân
--   mỗi product chỉ có một pitch_type = pitch_type của toàn bộ sân con
--   pitch_time                        = giờ hoạt động của cụm sân
--   subpitch_available_time           = cùng bộ giờ với cụm sân cha
-- ============================================================

-- ----------------------------------------------------------------
-- 1. PRODUCTS
-- ----------------------------------------------------------------
INSERT INTO `products`
  (`name`, `price`, `image`, `detail_desc`, `short_desc`, `quantity`,
   `sale`, `address`, `address_detail`, `status`, `user_id`)
VALUES
(
  'Saigon Sports Hub',
  220000,
  'https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=800',
  'Cụm sân 5 người sử dụng cỏ nhân tạo tiêu chuẩn, hệ thống đèn LED chống chói, lưới chắn bóng cao và khu thay đồ sạch sẽ. Có bãi giữ xe, nước uống và khu vực chờ cho đội bóng.',
  'Cụm 3 sân 5 người hiện đại tại Quận 10',
  3, 5,
  'Quận 10, TP.HCM',
  '268 Tô Hiến Thành, Phường 15, Quận 10, TP.HCM',
  'ACTIVE', (SELECT `id` FROM `user` WHERE `email` = 'admin@antigravity.vn' LIMIT 1)
);
SET @v10_product_1 = LAST_INSERT_ID();

INSERT INTO `products`
  (`name`, `price`, `image`, `detail_desc`, `short_desc`, `quantity`,
   `sale`, `address`, `address_detail`, `status`, `user_id`)
VALUES
(
  'Thảo Điền Football Arena',
  380000,
  'https://images.unsplash.com/photo-1459865264687-595d652de67e?w=800',
  'Hai sân 7 người mặt cỏ dày, thoát nước nhanh và có khán đài nhỏ. Sân phù hợp cho giao hữu, luyện tập câu lạc bộ và giải phong trào vào buổi tối.',
  'Cụm 2 sân 7 người cao cấp tại Thảo Điền',
  2, 0,
  'TP. Thủ Đức, TP.HCM',
  '39 Đường Xuân Thủy, Phường Thảo Điền, TP. Thủ Đức, TP.HCM',
  'ACTIVE', (SELECT `id` FROM `user` WHERE `email` = 'admin@antigravity.vn' LIMIT 1)
);
SET @v10_product_2 = LAST_INSERT_ID();

INSERT INTO `products`
  (`name`, `price`, `image`, `detail_desc`, `short_desc`, `quantity`,
   `sale`, `address`, `address_detail`, `status`, `user_id`)
VALUES
(
  'Phú Nhuận Football Park',
  200000,
  'https://images.unsplash.com/photo-1553778263-73a83bab9b0c?w=800',
  'Cụm sân mini nằm gần trung tâm, sử dụng cỏ nhân tạo mềm và đèn chiếu sáng đồng đều. Khuôn viên có mái che khu chờ, phòng thay đồ và quầy nước phục vụ các đội.',
  'Cụm 4 sân 5 người thuận tiện gần trung tâm',
  4, 10,
  'Quận Phú Nhuận, TP.HCM',
  '18 Đường Hoa Sứ, Phường 7, Quận Phú Nhuận, TP.HCM',
  'ACTIVE', (SELECT `id` FROM `user` WHERE `email` = 'admin@antigravity.vn' LIMIT 1)
);
SET @v10_product_3 = LAST_INSERT_ID();

INSERT INTO `products`
  (`name`, `price`, `image`, `detail_desc`, `short_desc`, `quantity`,
   `sale`, `address`, `address_detail`, `status`, `user_id`)
VALUES
(
  'Tân Bình Victory Field',
  360000,
  'https://images.unsplash.com/photo-1526232761682-d26e03ac148e?w=800',
  'Ba sân 7 người có kích thước đồng đều, mặt cỏ giảm chấn và hệ thống thoát nước tốt. Cụm sân có bãi xe rộng, phòng trọng tài và bảng tỷ số phục vụ giải đấu phong trào.',
  'Cụm 3 sân 7 người dành cho thi đấu phong trào',
  3, 5,
  'Quận Tân Bình, TP.HCM',
  '2A Phan Thúc Duyện, Phường 4, Quận Tân Bình, TP.HCM',
  'ACTIVE', (SELECT `id` FROM `user` WHERE `email` = 'admin@antigravity.vn' LIMIT 1)
);
SET @v10_product_4 = LAST_INSERT_ID();

INSERT INTO `products`
  (`name`, `price`, `image`, `detail_desc`, `short_desc`, `quantity`,
   `sale`, `address`, `address_detail`, `status`, `user_id`)
VALUES
(
  'Gò Vấp Green Pitch',
  180000,
  'https://tse2.mm.bing.net/th/id/OIP.ellWQiZsdyhEUxxAacrYVwHaHa?r=0&rs=1&pid=ImgDetMain&o=7&rm=3',
  'Năm sân 5 người phù hợp cho học sinh, sinh viên và đội bóng công ty. Mặt sân được bảo dưỡng định kỳ, có đèn LED, lưới bao quanh và khu gửi xe miễn phí.',
  'Cụm 5 sân 5 người giá tốt tại Gò Vấp',
  5, 0,
  'Quận Gò Vấp, TP.HCM',
  '521 Phan Văn Trị, Phường 5, Quận Gò Vấp, TP.HCM',
  'ACTIVE', (SELECT `id` FROM `user` WHERE `email` = 'admin@antigravity.vn' LIMIT 1)
);
SET @v10_product_5 = LAST_INSERT_ID();

INSERT INTO `products`
  (`name`, `price`, `image`, `detail_desc`, `short_desc`, `quantity`,
   `sale`, `address`, `address_detail`, `status`, `user_id`)
VALUES
(
  'Bình Thạnh Riverside Arena',
  400000,
  'https://conhantaofifa.com/wp-content/uploads/2024/06/san-bong-dang-khoi.jpg',
  'Cụm sân 7 người thoáng mát gần bờ sông, mặt cỏ đàn hồi tốt và hệ thống chiếu sáng công suất cao. Có khu khởi động, phòng tắm và khu vực ngồi xem riêng.',
  'Cụm 2 sân 7 người thoáng mát gần bờ sông',
  2, 8,
  'Quận Bình Thạnh, TP.HCM',
  '1143 Bình Quới, Phường 28, Quận Bình Thạnh, TP.HCM',
  'ACTIVE', (SELECT `id` FROM `user` WHERE `email` = 'admin@antigravity.vn' LIMIT 1)
);
SET @v10_product_6 = LAST_INSERT_ID();

INSERT INTO `products`
  (`name`, `price`, `image`, `detail_desc`, `short_desc`, `quantity`,
   `sale`, `address`, `address_detail`, `status`, `user_id`)
VALUES
(
  'Thủ Đức Champions Pitch',
  210000,
  'https://images.unsplash.com/photo-1517466787929-bc90951d0974?w=800',
  'Bốn sân mini có mặt cỏ mới, khung thành chắc chắn và lưới chắn bóng cao. Sân có khu thay đồ, căn tin và không gian chờ rộng, phù hợp đặt lịch thường xuyên.',
  'Cụm 4 sân 5 người dành cho đội bóng trẻ',
  4, 12,
  'TP. Thủ Đức, TP.HCM',
  '17 Đường Số 8, Phường Linh Trung, TP. Thủ Đức, TP.HCM',
  'ACTIVE', (SELECT `id` FROM `user` WHERE `email` = 'admin@antigravity.vn' LIMIT 1)
);
SET @v10_product_7 = LAST_INSERT_ID();

INSERT INTO `products`
  (`name`, `price`, `image`, `detail_desc`, `short_desc`, `quantity`,
   `sale`, `address`, `address_detail`, `status`, `user_id`)
VALUES
(
  'Quận 7 Premier Park',
  420000,
  'https://images.unsplash.com/photo-1529900748604-07564a03e7a6?w=800',
  'Ba sân 7 người tiêu chuẩn thi đấu phong trào với mặt cỏ cao cấp, đèn LED và khu kỹ thuật hai bên sân. Cụm sân có phòng tắm, tủ đồ và bãi đỗ ô tô.',
  'Cụm 3 sân 7 người tiêu chuẩn tại Quận 7',
  3, 10,
  'Quận 7, TP.HCM',
  '72 Đường Hoàng Quốc Việt, Phường Phú Mỹ, Quận 7, TP.HCM',
  'ACTIVE', (SELECT `id` FROM `user` WHERE `email` = 'admin@antigravity.vn' LIMIT 1)
);
SET @v10_product_8 = LAST_INSERT_ID();

INSERT INTO `products`
  (`name`, `price`, `image`, `detail_desc`, `short_desc`, `quantity`,
   `sale`, `address`, `address_detail`, `status`, `user_id`)
VALUES
(
  'Bình Tân Star Football',
  190000,
  'https://turfcaresports.com.vn/media/ckeditor_uploads/2022/03/21/z3277035284394_5bb5ffd71ff16f16162713de355f5f69.jpg',
  'Cụm bốn sân mini có mặt cỏ êm, hệ thống chiếu sáng tiết kiệm điện và lưới chắn bóng an toàn. Có khu vực nghỉ giữa trận, quầy nước và bãi giữ xe máy rộng.',
  'Cụm 4 sân 5 người năng động tại Bình Tân',
  4, 5,
  'Quận Bình Tân, TP.HCM',
  '88 Đường Số 7, Phường Bình Trị Đông B, Quận Bình Tân, TP.HCM',
  'ACTIVE', (SELECT `id` FROM `user` WHERE `email` = 'admin@antigravity.vn' LIMIT 1)
);
SET @v10_product_9 = LAST_INSERT_ID();

INSERT INTO `products`
  (`name`, `price`, `image`, `detail_desc`, `short_desc`, `quantity`,
   `sale`, `address`, `address_detail`, `status`, `user_id`)
VALUES
(
  'Hóc Môn Community Stadium',
  320000,
  'https://images.unsplash.com/photo-1522778119026-d647f0596c20?w=800',
  'Ba sân 7 người có không gian rộng, thông thoáng và mặt cỏ được chăm sóc thường xuyên. Cụm sân phù hợp cho các giải cộng đồng, có khu khán giả và bãi xe lớn.',
  'Cụm 3 sân 7 người rộng rãi tại Hóc Môn',
  3, 0,
  'Huyện Hóc Môn, TP.HCM',
  '25 Song Hành, Thị trấn Hóc Môn, Huyện Hóc Môn, TP.HCM',
  'ACTIVE', (SELECT `id` FROM `user` WHERE `email` = 'admin@antigravity.vn' LIMIT 1)
);
SET @v10_product_10 = LAST_INSERT_ID();

-- ----------------------------------------------------------------
-- 2. PITCH_TIME
-- Lấy ID theo giá trị time thay vì giả định ID cố định của available_time.
-- ----------------------------------------------------------------
INSERT INTO `pitch_time` (`product_id`, `time_id`)
SELECT @v10_product_1, `id` FROM `available_time` WHERE `time` BETWEEN '05:00:00' AND '22:00:00'
UNION ALL SELECT @v10_product_2, `id` FROM `available_time` WHERE `time` BETWEEN '06:00:00' AND '22:00:00'
UNION ALL SELECT @v10_product_3, `id` FROM `available_time` WHERE `time` BETWEEN '05:00:00' AND '22:00:00'
UNION ALL SELECT @v10_product_4, `id` FROM `available_time` WHERE `time` BETWEEN '06:00:00' AND '22:00:00'
UNION ALL SELECT @v10_product_5, `id` FROM `available_time` WHERE `time` BETWEEN '05:00:00' AND '22:00:00'
UNION ALL SELECT @v10_product_6, `id` FROM `available_time` WHERE `time` BETWEEN '06:00:00' AND '21:00:00'
UNION ALL SELECT @v10_product_7, `id` FROM `available_time` WHERE `time` BETWEEN '05:00:00' AND '22:00:00'
UNION ALL SELECT @v10_product_8, `id` FROM `available_time` WHERE `time` BETWEEN '06:00:00' AND '22:00:00'
UNION ALL SELECT @v10_product_9, `id` FROM `available_time` WHERE `time` BETWEEN '05:00:00' AND '22:00:00'
UNION ALL SELECT @v10_product_10, `id` FROM `available_time` WHERE `time` BETWEEN '06:00:00' AND '21:00:00';

-- ----------------------------------------------------------------
-- 3. SUB_PITCHES
-- Tất cả sân con trong cùng một product có cùng pitch_type (Approach C).
-- ----------------------------------------------------------------
INSERT INTO `sub_pitches` (`name`, `pitch_type`, `product_id`) VALUES
('Sân A1', 'FIVE_ASIDE', @v10_product_1),
('Sân A2', 'FIVE_ASIDE', @v10_product_1),
('Sân A3', 'FIVE_ASIDE', @v10_product_1),

('Sân Riverside A', 'SEVEN_ASIDE', @v10_product_2),
('Sân Riverside B', 'SEVEN_ASIDE', @v10_product_2),

('Sân Hoa 1', 'FIVE_ASIDE', @v10_product_3),
('Sân Hoa 2', 'FIVE_ASIDE', @v10_product_3),
('Sân Hoa 3', 'FIVE_ASIDE', @v10_product_3),
('Sân Hoa 4', 'FIVE_ASIDE', @v10_product_3),

('Sân Victory A', 'SEVEN_ASIDE', @v10_product_4),
('Sân Victory B', 'SEVEN_ASIDE', @v10_product_4),
('Sân Victory C', 'SEVEN_ASIDE', @v10_product_4),

('Sân Green 1', 'FIVE_ASIDE', @v10_product_5),
('Sân Green 2', 'FIVE_ASIDE', @v10_product_5),
('Sân Green 3', 'FIVE_ASIDE', @v10_product_5),
('Sân Green 4', 'FIVE_ASIDE', @v10_product_5),
('Sân Green 5', 'FIVE_ASIDE', @v10_product_5),

('Sân River A', 'SEVEN_ASIDE', @v10_product_6),
('Sân River B', 'SEVEN_ASIDE', @v10_product_6),

('Sân Champions 1', 'FIVE_ASIDE', @v10_product_7),
('Sân Champions 2', 'FIVE_ASIDE', @v10_product_7),
('Sân Champions 3', 'FIVE_ASIDE', @v10_product_7),
('Sân Champions 4', 'FIVE_ASIDE', @v10_product_7),

('Sân Premier A', 'SEVEN_ASIDE', @v10_product_8),
('Sân Premier B', 'SEVEN_ASIDE', @v10_product_8),
('Sân Premier C', 'SEVEN_ASIDE', @v10_product_8),

('Sân Star 1', 'FIVE_ASIDE', @v10_product_9),
('Sân Star 2', 'FIVE_ASIDE', @v10_product_9),
('Sân Star 3', 'FIVE_ASIDE', @v10_product_9),
('Sân Star 4', 'FIVE_ASIDE', @v10_product_9),

('Sân Community A', 'SEVEN_ASIDE', @v10_product_10),
('Sân Community B', 'SEVEN_ASIDE', @v10_product_10),
('Sân Community C', 'SEVEN_ASIDE', @v10_product_10);

-- ----------------------------------------------------------------
-- 4. SUBPITCH_AVAILABLE_TIME
-- Sao chép chính xác lịch của product cha sang từng sân con.
-- ----------------------------------------------------------------
INSERT INTO `subpitch_available_time` (`sub_pitch_id`, `available_time_id`)
SELECT sp.`id`, pt.`time_id`
FROM `sub_pitches` sp
JOIN `pitch_time` pt ON pt.`product_id` = sp.`product_id`
WHERE sp.`product_id` IN (
  @v10_product_1, @v10_product_2, @v10_product_3, @v10_product_4, @v10_product_5,
  @v10_product_6, @v10_product_7, @v10_product_8, @v10_product_9, @v10_product_10
);
