-- ============================================================
-- V6__seed_pickleball_accessories.sql
-- Bổ sung 20 mẫu vợt pickleball (racket id 9-28) cho thuê.
-- Entity hiện tại dùng Racket làm phụ kiện của từng địa điểm sân.
-- ============================================================

INSERT INTO `racket`
  (`id`, `name`, `price`, `available`, `factory`, `image`,
   `rental_price_per_day`, `rental_price_per_play`,
   `booking_stock_quantity`, `quantity`, `status`, `product_id`)
VALUES
(9,  'JOOLA Ben Johns Perseus 3S 16mm', 6200000, b'1', 'JOOLA',
 'https://joola.com/cdn/shop/files/3_0051_2R1A6092-93_6657000a-447e-4c94-9c41-094771baf777.jpg?v=1766505252&width=800', 120000, 45000, 8, 8, 'ACTIVE', 4),
(10, 'Selkirk SLK Halo Control XL', 3200000, b'1', 'Selkirk',
 'https://cdn.shopify.com/s/files/1/0152/5763/2822/files/HALO_CONTROL_XL_16mm_BLUE_01_c9fb0f02-42f6-4a6a-aaf9-7b56e89afe76.jpg?v=1724171431', 75000, 30000, 10, 10, 'ACTIVE', 5),
(11, 'Paddletek Bantam TKO-CX', 5800000, b'1', 'Paddletek',
 'https://www.paddletek.com/cdn/shop/files/Paddletek_Bantam_TKO-CX_Ocean_12.7_Front.png?v=1773082995&width=1200', 110000, 40000, 6, 6, 'ACTIVE', 6),
(12, 'CRBN 1X Power Series 16mm', 5600000, b'1', 'CRBN',
 'https://crbnpickleball.com/cdn/shop/files/CRBN_Power_1X_03.jpg?v=1771601486&width=1080', 105000, 40000, 6, 6, 'ACTIVE', 7),
(13, 'Gearbox Pro Power Elongated', 6500000, b'1', 'Gearbox',
 'https://gearboxsports.com/cdn/shop/files/1PROEPU1-1_Gearbox_Pro_Elongated_Ultimate-1.jpg?v=1776058264', 125000, 50000, 5, 5, 'ACTIVE', 8),
(14, 'Vatic Pro Prism Flash 16mm', 2700000, b'1', 'Vatic Pro',
 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/71/A_pickleball_paddle_with_two_pickleballs.jpg/960px-A_pickleball_paddle_with_two_pickleballs.jpg', 65000, 25000, 10, 10, 'ACTIVE', 9),
(15, 'Six Zero Double Black Diamond', 4300000, b'1', 'Six Zero',
 'https://upload.wikimedia.org/wikipedia/commons/4/49/Headpickleballracket.jpg', 90000, 35000, 7, 7, 'ACTIVE', 10),
(16, 'Engage Pursuit Pro1 6.0', 5900000, b'1', 'Engage',
 'https://upload.wikimedia.org/wikipedia/commons/thumb/c/ca/Pickleball_balls_and_paddles.jpg/960px-Pickleball_balls_and_paddles.jpg', 115000, 45000, 5, 5, 'ACTIVE', 11),
(17, 'Franklin FS Tour Dynasty 16mm', 3900000, b'1', 'Franklin',
 'https://upload.wikimedia.org/wikipedia/commons/thumb/2/20/Pickleball_brown_dotted_paddle4.jpg/960px-Pickleball_brown_dotted_paddle4.jpg', 85000, 32000, 8, 8, 'ACTIVE', 12),
(18, 'ONIX Evoke Premier Pro', 3600000, b'1', 'ONIX',
 'https://cdn.shopify.com/s/files/1/0481/9828/7516/files/KZ1141-PNK_Evoke_Premier_Pink-8896__KZ1141-PNK-1.png?v=1750875377', 80000, 30000, 8, 8, 'ACTIVE', 13),
(19, 'Diadem Edge 18K Power Pro', 5400000, b'1', 'Diadem',
 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/06/Pickleball_colored_paddle5.jpg/960px-Pickleball_colored_paddle5.jpg', 105000, 40000, 6, 6, 'ACTIVE', 14),
(20, 'ProKennex Black Ace Pro', 5200000, b'1', 'ProKennex',
 'https://upload.wikimedia.org/wikipedia/commons/4/4c/ProKennex_Ovation_Flight_2022.png', 100000, 38000, 6, 6, 'ACTIVE', 15),
(21, 'Electrum Model E Elite', 4700000, b'1', 'Electrum',
 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/e7/Pickleball_one_paddle3.jpg/960px-Pickleball_one_paddle3.jpg', 95000, 35000, 7, 7, 'ACTIVE', 16),
(22, 'Volair Mach 2 Forza 16mm', 4500000, b'1', 'Volair',
 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/94/Pickleball_Paddle_Materials_Exploring_T700_Raw_Carbon_Technology.png/960px-Pickleball_Paddle_Materials_Exploring_T700_Raw_Carbon_Technology.png', 90000, 35000, 7, 7, 'ACTIVE', 17),
(23, 'Bread & Butter Filth 16mm', 4100000, b'1', 'Bread & Butter',
 'https://upload.wikimedia.org/wikipedia/commons/thumb/b/b1/Pickleball_three_paddles1.jpg/960px-Pickleball_three_paddles1.jpg', 85000, 32000, 8, 8, 'ACTIVE', 18),
(24, 'Ronbus R3 Pulsar', 3500000, b'1', 'Ronbus',
 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/7a/Sandy_Pickle_pickleball_paddles_and_balls_on_the_beach.jpg/960px-Sandy_Pickle_pickleball_paddles_and_balls_on_the_beach.jpg', 75000, 28000, 8, 8, 'ACTIVE', 19),
(25, 'Head Radical Tour Raw EX', 3300000, b'1', 'Head',
 'https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/%C3%9Ct%C5%91k_%C3%A9s_labda.jpg/960px-%C3%9Ct%C5%91k_%C3%A9s_labda.jpg', 70000, 27000, 9, 9, 'ACTIVE', 20),
(26, 'Wilson Blaze Tour 16', 3000000, b'1', 'Wilson',
 'https://images.unsplash.com/photo-1753901821774-22a88913130f?auto=format&fit=crop&w=800&q=80', 65000, 25000, 10, 10, 'ACTIVE', 21),
(27, 'Gamma Obsidian 16', 2900000, b'1', 'Gamma',
 'https://images.unsplash.com/photo-1769911112258-47da2e8eee67?auto=format&fit=crop&w=800&q=80', 60000, 25000, 10, 10, 'ACTIVE', 22),
(28, 'Holbrook Mav Pro 16mm', 3800000, b'1', 'Holbrook',
 'https://images.unsplash.com/photo-1762423570127-c36ff11b883f?auto=format&fit=crop&w=800&q=80', 80000, 30000, 8, 8, 'ACTIVE', 23);

-- Khởi tạo tồn kho 7 ngày để phụ kiện mới có thể được thuê ngay.
INSERT INTO `racket_stock_by_date`
  (`racket_id`, `date`, `available_stock`, `reserved_stock`, `rental_stock`,
   `total_stock`, `created_at`, `updated_at`)
SELECT r.`id`,
       DATE_ADD(CURDATE(), INTERVAL (d.`id` - 1) DAY),
       r.`quantity`, 0, 0, r.`quantity`, NOW(), NOW()
FROM `racket` r
JOIN `available_time` d ON d.`id` BETWEEN 1 AND 7
WHERE r.`id` BETWEEN 9 AND 28;
