-- ============================================================
-- V5__seed_equipment_stock.sql
-- Khởi tạo kho thiết bị cho thuê cho 7 ngày (hôm nay → hôm nay + 6).
-- Window này đồng bộ với generateStockByDate() trong
-- EquipmentStockByDateService (targetDate = today.plusDays(6)).
-- Scheduler chạy hàng đêm lúc 00:00 sẽ tự sinh thêm ngày mới.
-- INSERT IGNORE kết hợp với UNIQUE KEY (equipment_id, date) trên
-- bảng đảm bảo chạy lại không tạo bản ghi trùng.
-- ============================================================

INSERT IGNORE INTO `equipment_stock_by_date`
  (`equipment_id`, `date`, `available_stock`, `reserved_stock`, `rental_stock`, `total_stock`, `created_at`, `updated_at`)
VALUES

-- ================================================================
-- Equipment 1: Bóng Adidas FIFA Pro size 5 (qty = 10, product_id = 1)
-- ================================================================
(1, CURDATE(),                    10, 0, 0, 10, NOW(), NOW()),
(1, CURDATE() + INTERVAL 1 DAY,   10, 0, 0, 10, NOW(), NOW()),
(1, CURDATE() + INTERVAL 2 DAY,   10, 0, 0, 10, NOW(), NOW()),
(1, CURDATE() + INTERVAL 3 DAY,   10, 0, 0, 10, NOW(), NOW()),
(1, CURDATE() + INTERVAL 4 DAY,   10, 0, 0, 10, NOW(), NOW()),
(1, CURDATE() + INTERVAL 5 DAY,   10, 0, 0, 10, NOW(), NOW()),
(1, CURDATE() + INTERVAL 6 DAY,   10, 0, 0, 10, NOW(), NOW()),

-- ================================================================
-- Equipment 2: Áo bib set 10 cái (qty = 8, product_id = 1)
-- ================================================================
(2, CURDATE(),                     8, 0, 0,  8, NOW(), NOW()),
(2, CURDATE() + INTERVAL 1 DAY,    8, 0, 0,  8, NOW(), NOW()),
(2, CURDATE() + INTERVAL 2 DAY,    8, 0, 0,  8, NOW(), NOW()),
(2, CURDATE() + INTERVAL 3 DAY,    8, 0, 0,  8, NOW(), NOW()),
(2, CURDATE() + INTERVAL 4 DAY,    8, 0, 0,  8, NOW(), NOW()),
(2, CURDATE() + INTERVAL 5 DAY,    8, 0, 0,  8, NOW(), NOW()),
(2, CURDATE() + INTERVAL 6 DAY,    8, 0, 0,  8, NOW(), NOW()),

-- ================================================================
-- Equipment 3: Lưới khung thành mini (qty = 6, product_id = 1)
-- ================================================================
(3, CURDATE(),                     6, 0, 0,  6, NOW(), NOW()),
(3, CURDATE() + INTERVAL 1 DAY,    6, 0, 0,  6, NOW(), NOW()),
(3, CURDATE() + INTERVAL 2 DAY,    6, 0, 0,  6, NOW(), NOW()),
(3, CURDATE() + INTERVAL 3 DAY,    6, 0, 0,  6, NOW(), NOW()),
(3, CURDATE() + INTERVAL 4 DAY,    6, 0, 0,  6, NOW(), NOW()),
(3, CURDATE() + INTERVAL 5 DAY,    6, 0, 0,  6, NOW(), NOW()),
(3, CURDATE() + INTERVAL 6 DAY,    6, 0, 0,  6, NOW(), NOW()),

-- ================================================================
-- Equipment 4: Bóng Nike Premier League (qty = 8, product_id = 2)
-- ================================================================
(4, CURDATE(),                     8, 0, 0,  8, NOW(), NOW()),
(4, CURDATE() + INTERVAL 1 DAY,    8, 0, 0,  8, NOW(), NOW()),
(4, CURDATE() + INTERVAL 2 DAY,    8, 0, 0,  8, NOW(), NOW()),
(4, CURDATE() + INTERVAL 3 DAY,    8, 0, 0,  8, NOW(), NOW()),
(4, CURDATE() + INTERVAL 4 DAY,    8, 0, 0,  8, NOW(), NOW()),
(4, CURDATE() + INTERVAL 5 DAY,    8, 0, 0,  8, NOW(), NOW()),
(4, CURDATE() + INTERVAL 6 DAY,    8, 0, 0,  8, NOW(), NOW()),

-- ================================================================
-- Equipment 5: Giày đinh dăm Adidas X Speedflow (qty = 6, product_id = 2)
-- ================================================================
(5, CURDATE(),                     6, 0, 0,  6, NOW(), NOW()),
(5, CURDATE() + INTERVAL 1 DAY,    6, 0, 0,  6, NOW(), NOW()),
(5, CURDATE() + INTERVAL 2 DAY,    6, 0, 0,  6, NOW(), NOW()),
(5, CURDATE() + INTERVAL 3 DAY,    6, 0, 0,  6, NOW(), NOW()),
(5, CURDATE() + INTERVAL 4 DAY,    6, 0, 0,  6, NOW(), NOW()),
(5, CURDATE() + INTERVAL 5 DAY,    6, 0, 0,  6, NOW(), NOW()),
(5, CURDATE() + INTERVAL 6 DAY,    6, 0, 0,  6, NOW(), NOW());
