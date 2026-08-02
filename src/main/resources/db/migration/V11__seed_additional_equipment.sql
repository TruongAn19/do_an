-- ============================================================
-- V11__seed_additional_equipment.sql
-- Thêm 5 phụ kiện cho các sân cha được seed ở V10.
--
-- Khớp với entity Equipment:
--   quantity                = tổng số lượng thiết bị
--   booking_stock_quantity  = số lượng khả dụng cho thuê kèm booking
--   rental_price_per_day    = giá thuê mang về theo ngày
--   rental_price_per_play   = giá thuê tại sân theo trận
-- ============================================================

INSERT INTO `equipment`
  (`name`, `price`, `available`, `factory`, `image`,
   `rental_price_per_day`, `rental_price_per_play`,
   `booking_stock_quantity`, `quantity`, `status`, `product_id`)
SELECT
  'Bóng Động Lực UHV 2.05', 850000, b'1', 'Động Lực',
  'https://tse3.mm.bing.net/th/id/OIP.fQPdYCC1lkkaRxD3auJkOQHaHa?r=0&rs=1&pid=ImgDetMain&o=7&rm=3',
  45000, 20000, 12, 12, 'ACTIVE', p.`id`
FROM `products` p
WHERE p.`name` = 'Saigon Sports Hub'
  AND NOT EXISTS (
    SELECT 1 FROM `equipment` e
    WHERE e.`product_id` = p.`id` AND e.`name` = 'Bóng Động Lực UHV 2.05'
  )
LIMIT 1;

INSERT INTO `equipment`
  (`name`, `price`, `available`, `factory`, `image`,
   `rental_price_per_day`, `rental_price_per_play`,
   `booking_stock_quantity`, `quantity`, `status`, `product_id`)
SELECT
  'Áo bib Mitre set 10 chiếc', 650000, b'1', 'Mitre',
  'https://down-vn.img.susercontent.com/file/vn-11134207-7r98o-lx2f2m901x3vb6',
  35000, 15000, 8, 8, 'ACTIVE', p.`id`
FROM `products` p
WHERE p.`name` = 'Thảo Điền Football Arena'
  AND NOT EXISTS (
    SELECT 1 FROM `equipment` e
    WHERE e.`product_id` = p.`id` AND e.`name` = 'Áo bib Mitre set 10 chiếc'
  )
LIMIT 1;

INSERT INTO `equipment`
  (`name`, `price`, `available`, `factory`, `image`,
   `rental_price_per_day`, `rental_price_per_play`,
   `booking_stock_quantity`, `quantity`, `status`, `product_id`)
SELECT
  'Găng tay thủ môn Adidas Predator', 1400000, b'1', 'Adidas',
  'https://vinsport.vn/vin_content/uploads/2024/01/Gang-bao-tay-thu-mon-Adidas-Predator-19-Pro-nhieu-mau-moi-ben-dep-ban-thiet-ke-moi-2024-cam-trang-2.jpg',
  60000, 25000, 6, 6, 'ACTIVE', p.`id`
FROM `products` p
WHERE p.`name` = 'Phú Nhuận Football Park'
  AND NOT EXISTS (
    SELECT 1 FROM `equipment` e
    WHERE e.`product_id` = p.`id` AND e.`name` = 'Găng tay thủ môn Adidas Predator'
  )
LIMIT 1;

INSERT INTO `equipment`
  (`name`, `price`, `available`, `factory`, `image`,
   `rental_price_per_day`, `rental_price_per_play`,
   `booking_stock_quantity`, `quantity`, `status`, `product_id`)
SELECT
  'Giày Nike Mercurial Vapor 15', 2800000, b'1', 'Nike',
  'https://product.hstatic.net/200000278317/product/giay-da-bong-nike-zoom-mercurial-vapor-15-pro-tf-dj5605-600-trang-do-1_293c8f663e5b4469982d67537250ca8c_master.jpg',
  80000, 35000, 10, 10, 'ACTIVE', p.`id`
FROM `products` p
WHERE p.`name` = 'Tân Bình Victory Field'
  AND NOT EXISTS (
    SELECT 1 FROM `equipment` e
    WHERE e.`product_id` = p.`id` AND e.`name` = 'Giày Nike Mercurial Vapor 15'
  )
LIMIT 1;

INSERT INTO `equipment`
  (`name`, `price`, `available`, `factory`, `image`,
   `rental_price_per_day`, `rental_price_per_play`,
   `booking_stock_quantity`, `quantity`, `status`, `product_id`)
SELECT
  'Bộ cọc nón tập luyện 20 chiếc', 480000, b'1', 'Zocker',
  'https://down-vn.img.susercontent.com/file/vn-11134207-7ra0g-m77uqlq53efsd9',
  30000, 12000, 7, 7, 'ACTIVE', p.`id`
FROM `products` p
WHERE p.`name` = 'Gò Vấp Green Pitch'
  AND NOT EXISTS (
    SELECT 1 FROM `equipment` e
    WHERE e.`product_id` = p.`id` AND e.`name` = 'Bộ cọc nón tập luyện 20 chiếc'
  )
LIMIT 1;

-- Khởi tạo tồn kho theo ngày cho đúng cửa sổ 7 ngày mà
-- EquipmentStockByDateService đang sử dụng.
INSERT IGNORE INTO `equipment_stock_by_date`
  (`equipment_id`, `date`, `available_stock`, `reserved_stock`,
   `rental_stock`, `total_stock`, `created_at`, `updated_at`)
SELECT
  e.`id`,
  DATE_ADD(CURDATE(), INTERVAL offsets.`day_offset` DAY),
  e.`quantity`, 0, 0, e.`quantity`, NOW(), NOW()
FROM `equipment` e
JOIN `products` p ON p.`id` = e.`product_id`
JOIN (
  SELECT 0 AS `day_offset`
  UNION ALL SELECT 1
  UNION ALL SELECT 2
  UNION ALL SELECT 3
  UNION ALL SELECT 4
  UNION ALL SELECT 5
  UNION ALL SELECT 6
) offsets
WHERE (p.`name` = 'Saigon Sports Hub'
       AND e.`name` = 'Bóng Động Lực UHV 2.05')
   OR (p.`name` = 'Thảo Điền Football Arena'
       AND e.`name` = 'Áo bib Mitre set 10 chiếc')
   OR (p.`name` = 'Phú Nhuận Football Park'
       AND e.`name` = 'Găng tay thủ môn Adidas Predator')
   OR (p.`name` = 'Tân Bình Victory Field'
       AND e.`name` = 'Giày Nike Mercurial Vapor 15')
   OR (p.`name` = 'Gò Vấp Green Pitch'
       AND e.`name` = 'Bộ cọc nón tập luyện 20 chiếc');
