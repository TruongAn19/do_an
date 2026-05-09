-- 1. Copy toàn bộ địa chỉ hiện tại vào cột địa chỉ chi tiết (nếu cột chi tiết đang trống)
UPDATE products SET address_detail = address WHERE address_detail IS NULL OR address_detail = '';

-- 2. Nếu địa chỉ chi tiết có chứa "Hà Nội", đặt Khu vực là "Hà Nội"
UPDATE products SET address = 'Hà Nội' WHERE address_detail LIKE '%Hà Nội%';

-- 3. Nếu địa chỉ chi tiết có chứa "TP.HCM" hoặc "Quận", đặt Khu vực là "TP.HCM"
UPDATE products SET address = 'TP.HCM' WHERE address_detail LIKE '%TP.HCM%' OR address_detail LIKE '%Quận%';

-- 4. Xử lý cụ thể cho các sân mẫu nếu cần
UPDATE products SET address = 'TP.HCM' WHERE id IN (1, 2, 3);
