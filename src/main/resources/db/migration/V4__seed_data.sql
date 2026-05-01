-- ============================================================
-- V4__seed_data.sql  --  Dữ liệu khởi tạo hệ thống
-- Mật khẩu mặc định mọi tài khoản: 123456 (BCrypt)
-- ============================================================

-- ----------------------------------------------------------------
-- 1. ROLES
--    Lưu ý: KHÔNG có prefix "ROLE_" — CustomUserDetailsService
--    đã tự prepend "ROLE_" khi tạo SimpleGrantedAuthority
-- ----------------------------------------------------------------
INSERT IGNORE INTO `roles` (`id`, `name`, `description`) VALUES
(1, 'ADMIN', 'Quản trị viên hệ thống'),
(2, 'STAFF', 'Nhân viên quản lý sân'),
(3, 'USER',  'Khách hàng thường');

-- ----------------------------------------------------------------
-- 2. USERS
-- ----------------------------------------------------------------
INSERT IGNORE INTO `user`
  (`id`, `email`, `password`, `full_name`, `phone`, `member_level`, `role_id`)
VALUES
-- Admin
(1, 'admin@antigravity.vn',
    '$2a$10$E2UPv7arXmp3q0gnuHXGDu3ZkgHNfuqy7WvjiW6NeJwzU6odigMFO',
    'Admin Hệ Thống', '0901000001', 'NORMAL', 1),
-- Nhân viên
(2, 'staff@antigravity.vn',
    '$2a$10$E2UPv7arXmp3q0gnuHXGDu3ZkgHNfuqy7WvjiW6NeJwzU6odigMFO',
    'Nguyễn Văn Nhân', '0901000002', 'NORMAL', 2),
-- Demo users
(3, 'alice@gmail.com',
    '$2a$10$E2UPv7arXmp3q0gnuHXGDu3ZkgHNfuqy7WvjiW6NeJwzU6odigMFO',
    'Trần Thị Alice', '0901000003', 'SILVER', 3),
(4, 'bob@gmail.com',
    '$2a$10$E2UPv7arXmp3q0gnuHXGDu3ZkgHNfuqy7WvjiW6NeJwzU6odigMFO',
    'Lê Văn Bob', '0901000004', 'NORMAL', 3);

-- ----------------------------------------------------------------
-- 3. AVAILABLE TIMES  (khung giờ chia theo từng tiếng, 05:00–22:00)
--    id  1 = 05:00  …  id 18 = 22:00
-- ----------------------------------------------------------------
INSERT IGNORE INTO `available_time` (`id`, `time`) VALUES
( 1, '05:00:00'), ( 2, '06:00:00'), ( 3, '07:00:00'), ( 4, '08:00:00'),
( 5, '09:00:00'), ( 6, '10:00:00'), ( 7, '11:00:00'), ( 8, '12:00:00'),
( 9, '13:00:00'), (10, '14:00:00'), (11, '15:00:00'), (12, '16:00:00'),
(13, '17:00:00'), (14, '18:00:00'), (15, '19:00:00'), (16, '20:00:00'),
(17, '21:00:00'), (18, '22:00:00');

-- ----------------------------------------------------------------
-- 4. PRODUCTS  (cụm sân — mỗi product là 1 địa điểm)
--    quantity  = số sân con trong cụm
--    price     = giá thuê 1 sân / 1 giờ (VNĐ)
--    sale      = giảm giá (0 = không giảm)
-- ----------------------------------------------------------------
INSERT IGNORE INTO `products`
  (`id`, `name`, `price`, `image`, `detail_desc`, `short_desc`,
   `quantity`, `sale`, `address`, `deposit_price`, `status`, `user_id`)
VALUES
(
  1,
  'Sân Cầu Lông Antigravity',
  50000,
  'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=800',
  'Cụm 5 sân cầu lông tiêu chuẩn BWF. Thảm PVC chuyên dụng chống trơn trượt, hệ thống đèn LED 800 lux đảm bảo không bị chói mắt. Phòng thay đồ, tủ khóa và bãi đỗ xe miễn phí. Cho thuê vợt và cầu ngay tại sân.',
  'Cụm 5 sân chuẩn BWF — trung tâm Quận 1',
  5, 0,
  '123 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM',
  20000, 'ACTIVE', 1
),
(
  2,
  'Elite Arena Badminton',
  65000,
  'https://images.unsplash.com/photo-1599474924187-334a4ae5bd3a?w=800',
  'Cụm 4 sân cao cấp với sàn gỗ Taraflex nhập khẩu từ Pháp. Hệ thống điều hoà trung tâm, ghế khán giả 50 chỗ, camera an ninh 24/7. Phù hợp tổ chức giải đấu cấp câu lạc bộ và thi đấu giao lưu.',
  'Cụm 4 sân sàn gỗ cao cấp — Quận 3',
  4, 0,
  '456 Võ Văn Tần, Phường 5, Quận 3, TP.HCM',
  25000, 'ACTIVE', 1
),
(
  3,
  'Pro Center Badminton',
  80000,
  'https://images.unsplash.com/photo-1612872087720-bb876e2e67d1?w=800',
  'Cụm 3 sân thi đấu chuyên nghiệp theo tiêu chuẩn quốc tế, nơi thường xuyên tổ chức các giải đấu cấp thành phố. Sàn Taraflex Pro, đèn LED chuyên biệt 1000 lux, hệ thống tính điểm điện tử và phát sóng trực tiếp.',
  'Cụm 3 sân chuyên nghiệp chuẩn thi đấu — Quận 7',
  3, 0,
  '789 Lê Văn Lương, Phường Tân Hưng, Quận 7, TP.HCM',
  35000, 'ACTIVE', 1
);

-- ----------------------------------------------------------------
-- 5. COURT_TIME  (khung giờ hoạt động từng cụm sân)
--    Product 1 (Antigravity):  05:00–22:00  → time_id  1–18
--    Product 2 (Elite Arena):  06:00–21:00  → time_id  2–17
--    Product 3 (Pro Center):   07:00–22:00  → time_id  3–18
-- ----------------------------------------------------------------
INSERT IGNORE INTO `court_time` (`court_id`, `time_id`) VALUES
-- Antigravity: 05:00–22:00
(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),
(1,10),(1,11),(1,12),(1,13),(1,14),(1,15),(1,16),(1,17),(1,18),
-- Elite Arena: 06:00–21:00
(2,2),(2,3),(2,4),(2,5),(2,6),(2,7),(2,8),(2,9),(2,10),
(2,11),(2,12),(2,13),(2,14),(2,15),(2,16),(2,17),
-- Pro Center: 07:00–22:00
(3,3),(3,4),(3,5),(3,6),(3,7),(3,8),(3,9),(3,10),
(3,11),(3,12),(3,13),(3,14),(3,15),(3,16),(3,17),(3,18);

-- ----------------------------------------------------------------
-- 6. SUB_COURTS  (sân con trong từng cụm)
--    Product 1 → sub_court id  1–5  (Sân 1–5)
--    Product 2 → sub_court id  6–9  (Sân A–D)
--    Product 3 → sub_court id 10–12 (Sân Đỏ / Xanh / Vàng)
-- ----------------------------------------------------------------
INSERT IGNORE INTO `sub_courts` (`id`, `name`, `product_id`) VALUES
-- Antigravity (5 sân)
(1,  'Sân số 1', 1),
(2,  'Sân số 2', 1),
(3,  'Sân số 3', 1),
(4,  'Sân số 4', 1),
(5,  'Sân số 5', 1),
-- Elite Arena (4 sân)
(6,  'Sân A', 2),
(7,  'Sân B', 2),
(8,  'Sân C', 2),
(9,  'Sân D', 2),
-- Pro Center (3 sân)
(10, 'Sân Đỏ',  3),
(11, 'Sân Xanh', 3),
(12, 'Sân Vàng', 3);

-- ----------------------------------------------------------------
-- 7. SUBCOURT_AVAILABLE_TIME
--    Mỗi sân con được liên kết với đúng bộ khung giờ của cụm sân
--    mà nó thuộc về (khớp với court_time ở bước 5).
-- ----------------------------------------------------------------
INSERT IGNORE INTO `subcourt_available_time` (`sub_court_id`, `available_time_id`) VALUES
-- ── Sân 1 (Antigravity, 05:00–22:00, time 1–18) ──
(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),(1,11),(1,12),(1,13),(1,14),(1,15),(1,16),(1,17),(1,18),
-- ── Sân 2 ──
(2,1),(2,2),(2,3),(2,4),(2,5),(2,6),(2,7),(2,8),(2,9),(2,10),(2,11),(2,12),(2,13),(2,14),(2,15),(2,16),(2,17),(2,18),
-- ── Sân 3 ──
(3,1),(3,2),(3,3),(3,4),(3,5),(3,6),(3,7),(3,8),(3,9),(3,10),(3,11),(3,12),(3,13),(3,14),(3,15),(3,16),(3,17),(3,18),
-- ── Sân 4 ──
(4,1),(4,2),(4,3),(4,4),(4,5),(4,6),(4,7),(4,8),(4,9),(4,10),(4,11),(4,12),(4,13),(4,14),(4,15),(4,16),(4,17),(4,18),
-- ── Sân 5 ──
(5,1),(5,2),(5,3),(5,4),(5,5),(5,6),(5,7),(5,8),(5,9),(5,10),(5,11),(5,12),(5,13),(5,14),(5,15),(5,16),(5,17),(5,18),

-- ── Sân A (Elite Arena, 06:00–21:00, time 2–17) ──
(6,2),(6,3),(6,4),(6,5),(6,6),(6,7),(6,8),(6,9),(6,10),(6,11),(6,12),(6,13),(6,14),(6,15),(6,16),(6,17),
-- ── Sân B ──
(7,2),(7,3),(7,4),(7,5),(7,6),(7,7),(7,8),(7,9),(7,10),(7,11),(7,12),(7,13),(7,14),(7,15),(7,16),(7,17),
-- ── Sân C ──
(8,2),(8,3),(8,4),(8,5),(8,6),(8,7),(8,8),(8,9),(8,10),(8,11),(8,12),(8,13),(8,14),(8,15),(8,16),(8,17),
-- ── Sân D ──
(9,2),(9,3),(9,4),(9,5),(9,6),(9,7),(9,8),(9,9),(9,10),(9,11),(9,12),(9,13),(9,14),(9,15),(9,16),(9,17),

-- ── Sân Đỏ (Pro Center, 07:00–22:00, time 3–18) ──
(10,3),(10,4),(10,5),(10,6),(10,7),(10,8),(10,9),(10,10),(10,11),(10,12),(10,13),(10,14),(10,15),(10,16),(10,17),(10,18),
-- ── Sân Xanh ──
(11,3),(11,4),(11,5),(11,6),(11,7),(11,8),(11,9),(11,10),(11,11),(11,12),(11,13),(11,14),(11,15),(11,16),(11,17),(11,18),
-- ── Sân Vàng ──
(12,3),(12,4),(12,5),(12,6),(12,7),(12,8),(12,9),(12,10),(12,11),(12,12),(12,13),(12,14),(12,15),(12,16),(12,17),(12,18);

-- ----------------------------------------------------------------
-- 8. RACKETS  (vợt cho thuê — mỗi cụm sân có bộ vợt riêng)
--    quantity             = tổng số vợt
--    booking_stock_quantity = số vợt đưa vào pool cho thuê theo booking
--    rental_price_per_day = thuê mang về (tính theo ngày)
--    rental_price_per_play= thuê đánh tại sân (tính theo buổi)
-- ----------------------------------------------------------------
INSERT IGNORE INTO `racket`
  (`id`, `name`, `price`, `available`, `factory`, `image`,
   `rental_price_per_day`, `rental_price_per_play`,
   `booking_stock_quantity`, `quantity`, `status`, `product_id`)
VALUES
-- ── Antigravity (product_id = 1) ──
(1, 'Yonex Astrox 99 Play',    1500000, 1, 'Yonex',
    'https://images.unsplash.com/photo-1613532696025-10850d9cd808?w=400',
    50000, 20000, 10, 10, 'ACTIVE', 1),
(2, 'Victor Thruster K 9900',  1200000, 1, 'Victor',
    'https://images.unsplash.com/photo-1613532696025-10850d9cd808?w=400',
    40000, 15000,  8,  8, 'ACTIVE', 1),
(3, 'Li-Ning N9 II',            800000, 1, 'Li-Ning',
    'https://images.unsplash.com/photo-1613532696025-10850d9cd808?w=400',
    30000, 12000,  6,  6, 'ACTIVE', 1),

-- ── Elite Arena (product_id = 2) ──
(4, 'Yonex Nanoflare 800 Pro', 2000000, 1, 'Yonex',
    'https://images.unsplash.com/photo-1613532696025-10850d9cd808?w=400',
    60000, 25000,  8,  8, 'ACTIVE', 2),
(5, 'Victor Jetspeed S 12',    1500000, 1, 'Victor',
    'https://images.unsplash.com/photo-1613532696025-10850d9cd808?w=400',
    50000, 20000,  6,  6, 'ACTIVE', 2),

-- ── Pro Center (product_id = 3) ──
(6, 'Yonex Voltric Z Force II', 3500000, 1, 'Yonex',
    'https://images.unsplash.com/photo-1613532696025-10850d9cd808?w=400',
    80000, 35000,  5,  5, 'ACTIVE', 3),
(7, 'Li-Ning 3D Calibar 900B',  2500000, 1, 'Li-Ning',
    'https://images.unsplash.com/photo-1613532696025-10850d9cd808?w=400',
    70000, 30000,  4,  4, 'ACTIVE', 3),
(8, 'Apacs Feather Weight 55',   900000, 1, 'Apacs',
    'https://images.unsplash.com/photo-1613532696025-10850d9cd808?w=400',
    35000, 15000,  8,  8, 'ACTIVE', 3);
