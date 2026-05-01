-- ============================================================
-- V5__seed_racket_stock.sql
-- Khởi tạo kho vợt cho 7 ngày (hôm nay → hôm nay + 6).
-- Window này đồng bộ với generateStockByDate() trong
-- RacketStockByDateService (targetDate = today.plusDays(6)).
-- Scheduler chạy hàng đêm lúc 00:00 sẽ tự sinh thêm ngày mới.
-- INSERT IGNORE kết hợp với UNIQUE KEY (racket_id, date) trên
-- bảng đảm bảo chạy lại không tạo bản ghi trùng.
-- ============================================================

INSERT IGNORE INTO `racket_stock_by_date`
  (`racket_id`, `date`, `available_stock`, `reserved_stock`, `rental_stock`, `total_stock`, `created_at`, `updated_at`)
VALUES

-- ================================================================
-- Racket 1: Yonex Astrox 99 Play  (qty = 10, product_id = 1)
-- ================================================================
(1, CURDATE(),                    10, 0, 0, 10, NOW(), NOW()),
(1, CURDATE() + INTERVAL 1 DAY,   10, 0, 0, 10, NOW(), NOW()),
(1, CURDATE() + INTERVAL 2 DAY,   10, 0, 0, 10, NOW(), NOW()),
(1, CURDATE() + INTERVAL 3 DAY,   10, 0, 0, 10, NOW(), NOW()),
(1, CURDATE() + INTERVAL 4 DAY,   10, 0, 0, 10, NOW(), NOW()),
(1, CURDATE() + INTERVAL 5 DAY,   10, 0, 0, 10, NOW(), NOW()),
(1, CURDATE() + INTERVAL 6 DAY,   10, 0, 0, 10, NOW(), NOW()),

-- ================================================================
-- Racket 2: Victor Thruster K 9900  (qty = 8, product_id = 1)
-- ================================================================
(2, CURDATE(),                     8, 0, 0,  8, NOW(), NOW()),
(2, CURDATE() + INTERVAL 1 DAY,    8, 0, 0,  8, NOW(), NOW()),
(2, CURDATE() + INTERVAL 2 DAY,    8, 0, 0,  8, NOW(), NOW()),
(2, CURDATE() + INTERVAL 3 DAY,    8, 0, 0,  8, NOW(), NOW()),
(2, CURDATE() + INTERVAL 4 DAY,    8, 0, 0,  8, NOW(), NOW()),
(2, CURDATE() + INTERVAL 5 DAY,    8, 0, 0,  8, NOW(), NOW()),
(2, CURDATE() + INTERVAL 6 DAY,    8, 0, 0,  8, NOW(), NOW()),

-- ================================================================
-- Racket 3: Li-Ning N9 II  (qty = 6, product_id = 1)
-- ================================================================
(3, CURDATE(),                     6, 0, 0,  6, NOW(), NOW()),
(3, CURDATE() + INTERVAL 1 DAY,    6, 0, 0,  6, NOW(), NOW()),
(3, CURDATE() + INTERVAL 2 DAY,    6, 0, 0,  6, NOW(), NOW()),
(3, CURDATE() + INTERVAL 3 DAY,    6, 0, 0,  6, NOW(), NOW()),
(3, CURDATE() + INTERVAL 4 DAY,    6, 0, 0,  6, NOW(), NOW()),
(3, CURDATE() + INTERVAL 5 DAY,    6, 0, 0,  6, NOW(), NOW()),
(3, CURDATE() + INTERVAL 6 DAY,    6, 0, 0,  6, NOW(), NOW()),

-- ================================================================
-- Racket 4: Yonex Nanoflare 800 Pro  (qty = 8, product_id = 2)
-- ================================================================
(4, CURDATE(),                     8, 0, 0,  8, NOW(), NOW()),
(4, CURDATE() + INTERVAL 1 DAY,    8, 0, 0,  8, NOW(), NOW()),
(4, CURDATE() + INTERVAL 2 DAY,    8, 0, 0,  8, NOW(), NOW()),
(4, CURDATE() + INTERVAL 3 DAY,    8, 0, 0,  8, NOW(), NOW()),
(4, CURDATE() + INTERVAL 4 DAY,    8, 0, 0,  8, NOW(), NOW()),
(4, CURDATE() + INTERVAL 5 DAY,    8, 0, 0,  8, NOW(), NOW()),
(4, CURDATE() + INTERVAL 6 DAY,    8, 0, 0,  8, NOW(), NOW()),

-- ================================================================
-- Racket 5: Victor Jetspeed S 12  (qty = 6, product_id = 2)
-- ================================================================
(5, CURDATE(),                     6, 0, 0,  6, NOW(), NOW()),
(5, CURDATE() + INTERVAL 1 DAY,    6, 0, 0,  6, NOW(), NOW()),
(5, CURDATE() + INTERVAL 2 DAY,    6, 0, 0,  6, NOW(), NOW()),
(5, CURDATE() + INTERVAL 3 DAY,    6, 0, 0,  6, NOW(), NOW()),
(5, CURDATE() + INTERVAL 4 DAY,    6, 0, 0,  6, NOW(), NOW()),
(5, CURDATE() + INTERVAL 5 DAY,    6, 0, 0,  6, NOW(), NOW()),
(5, CURDATE() + INTERVAL 6 DAY,    6, 0, 0,  6, NOW(), NOW()),

-- ================================================================
-- Racket 6: Yonex Voltric Z Force II  (qty = 5, product_id = 3)
-- ================================================================
(6, CURDATE(),                     5, 0, 0,  5, NOW(), NOW()),
(6, CURDATE() + INTERVAL 1 DAY,    5, 0, 0,  5, NOW(), NOW()),
(6, CURDATE() + INTERVAL 2 DAY,    5, 0, 0,  5, NOW(), NOW()),
(6, CURDATE() + INTERVAL 3 DAY,    5, 0, 0,  5, NOW(), NOW()),
(6, CURDATE() + INTERVAL 4 DAY,    5, 0, 0,  5, NOW(), NOW()),
(6, CURDATE() + INTERVAL 5 DAY,    5, 0, 0,  5, NOW(), NOW()),
(6, CURDATE() + INTERVAL 6 DAY,    5, 0, 0,  5, NOW(), NOW()),

-- ================================================================
-- Racket 7: Li-Ning 3D Calibar 900B  (qty = 4, product_id = 3)
-- ================================================================
(7, CURDATE(),                     4, 0, 0,  4, NOW(), NOW()),
(7, CURDATE() + INTERVAL 1 DAY,    4, 0, 0,  4, NOW(), NOW()),
(7, CURDATE() + INTERVAL 2 DAY,    4, 0, 0,  4, NOW(), NOW()),
(7, CURDATE() + INTERVAL 3 DAY,    4, 0, 0,  4, NOW(), NOW()),
(7, CURDATE() + INTERVAL 4 DAY,    4, 0, 0,  4, NOW(), NOW()),
(7, CURDATE() + INTERVAL 5 DAY,    4, 0, 0,  4, NOW(), NOW()),
(7, CURDATE() + INTERVAL 6 DAY,    4, 0, 0,  4, NOW(), NOW()),

-- ================================================================
-- Racket 8: Apacs Feather Weight 55  (qty = 8, product_id = 3)
-- ================================================================
(8, CURDATE(),                     8, 0, 0,  8, NOW(), NOW()),
(8, CURDATE() + INTERVAL 1 DAY,    8, 0, 0,  8, NOW(), NOW()),
(8, CURDATE() + INTERVAL 2 DAY,    8, 0, 0,  8, NOW(), NOW()),
(8, CURDATE() + INTERVAL 3 DAY,    8, 0, 0,  8, NOW(), NOW()),
(8, CURDATE() + INTERVAL 4 DAY,    8, 0, 0,  8, NOW(), NOW()),
(8, CURDATE() + INTERVAL 5 DAY,    8, 0, 0,  8, NOW(), NOW()),
(8, CURDATE() + INTERVAL 6 DAY,    8, 0, 0,  8, NOW(), NOW());
