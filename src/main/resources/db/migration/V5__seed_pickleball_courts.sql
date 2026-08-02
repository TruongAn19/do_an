-- ============================================================
-- V5__seed_pickleball_courts.sql
-- Bổ sung 20 địa điểm pickleball (product id 4-23).
-- Mỗi địa điểm có sân con và khung giờ đặt sân tương ứng.
-- ============================================================

INSERT INTO `products`
  (`id`, `name`, `price`, `image`, `detail_desc`, `short_desc`,
   `quantity`, `sale`, `address`, `address_detail`, `deposit_price`, `status`, `user_id`)
VALUES
(4, 'Saigon Smash Pickleball', 110000,
 'https://www.canchas-deportivas.com/uploaded/mod_galeria/atenis3.jpg',
 'Cụm sân ngoài trời với mặt sân acrylic chống trượt, đèn LED và khu vực nghỉ có mái che. Có phòng thay đồ, nước uống và bãi giữ xe.',
 '4 sân acrylic ngoài trời tại Thủ Đức', 4, 5, 'TP.HCM',
 '25 Đường số 12, Phường An Khánh, TP. Thủ Đức, TP.HCM', 20000, 'ACTIVE', NULL),
(5, 'Thảo Điền Pickleball Hub', 160000,
 'https://5.imimg.com/data5/SELLER/Default/2022/6/VM/UI/OB/11336583/indiana-sports-outdoor-tennis-court-flooring-500x500.jpg',
 'Không gian pickleball cao cấp, mặt sân có lớp đệm giảm chấn, ánh sáng chống chói và khu lounge phục vụ người chơi.',
 '3 sân cao cấp có lớp đệm giảm chấn', 3, 0, 'TP.HCM',
 '88 Quốc Hương, Phường Thảo Điền, TP. Thủ Đức, TP.HCM', 30000, 'ACTIVE', NULL),
(6, 'Phú Nhuận Paddle Club', 125000,
 'https://5.imimg.com/data5/SELLER/Default/2022/6/VM/UI/OB/11336583/indiana-sports-outdoor-tennis-court-flooring-500x500.jpg',
 'Câu lạc bộ thân thiện dành cho người mới và nhóm gia đình, có huấn luyện viên, khu khởi động và cho thuê vợt tại quầy.',
 '5 sân dành cho gia đình và người mới', 5, 10, 'TP.HCM',
 '172 Phan Đăng Lưu, Phường 3, Quận Phú Nhuận, TP.HCM', 25000, 'ACTIVE', NULL),
(7, 'Tân Bình Ace Arena', 140000,
 'https://5.imimg.com/data5/SELLER/Default/2022/6/VM/UI/OB/11336583/indiana-sports-outdoor-tennis-court-flooring-500x500.jpg',
 'Cụm sân trong nhà trần cao, thông gió tốt, mặt sân tiêu chuẩn thi đấu và hệ thống đèn LED phủ đều toàn sân.',
 '4 sân trong nhà tiêu chuẩn thi đấu', 4, 0, 'TP.HCM',
 '36 Cộng Hòa, Phường 4, Quận Tân Bình, TP.HCM', 30000, 'ACTIVE', NULL),
(8, 'Riverside Pickleball Bình Thạnh', 130000,
 'https://images.unsplash.com/photo-1761644273884-83839f8f22e5?auto=format&fit=crop&w=1200&q=80',
 'Sân thoáng mát gần bờ sông, có mái che một phần, khu vực ghế chờ, tủ đồ cá nhân và quầy đồ uống thể thao.',
 '3 sân thoáng mát gần bờ sông', 3, 5, 'TP.HCM',
 '210 Nguyễn Hữu Cảnh, Phường 22, Quận Bình Thạnh, TP.HCM', 25000, 'ACTIVE', NULL),
(9, 'Hà Nội Capital Pickleball', 150000,
 'https://5.imimg.com/data5/SELLER/Default/2024/1/381246119/YZ/KC/DX/50260730/tennis-court-flooring-1000x1000.jpeg',
 'Trung tâm pickleball trong nhà, có điều hòa, sàn acrylic nhiều lớp và khu khán giả phù hợp tổ chức giải phong trào.',
 '6 sân trong nhà tại Cầu Giấy', 6, 0, 'Hà Nội',
 '15 Trần Thái Tông, Phường Dịch Vọng, Quận Cầu Giấy, Hà Nội', 30000, 'ACTIVE', NULL),
(10, 'West Lake Paddle House', 170000,
 'https://tse2.mm.bing.net/th/id/OIP.Pod9B8rVfyYENwBzgCCH_gHaHa?r=0&rs=1&pid=ImgDetMain&o=7&rm=3',
 'Câu lạc bộ gần Hồ Tây với mặt sân cushion, phòng tắm nóng lạnh, quầy cà phê và dịch vụ hướng dẫn kỹ thuật theo giờ.',
 '4 sân cushion cao cấp gần Hồ Tây', 4, 5, 'Hà Nội',
 '68 Võ Chí Công, Phường Xuân La, Quận Tây Hồ, Hà Nội', 35000, 'ACTIVE', NULL),
(11, 'Long Biên Pickleball Center', 115000,
 'https://i0.wp.com/jodhpursportsclub.com/wp-content/uploads/2023/03/sports_image-1.jpeg?fit=768%2C576&ssl=1',
 'Cụm sân rộng rãi, bãi đỗ ô tô miễn phí, hệ thống chiếu sáng ban đêm và khu vui chơi nhỏ dành cho trẻ em.',
 '5 sân rộng rãi, bãi đỗ xe miễn phí', 5, 0, 'Hà Nội',
 '120 Cổ Linh, Phường Long Biên, Quận Long Biên, Hà Nội', 20000, 'ACTIVE', NULL),
(12, 'Mỹ Đình Pro Pickleball', 155000,
 'https://www.meanwell.com/PBM/tw/img/portfolio/JR09-large.jpg',
 'Sân tập và thi đấu chuyên nghiệp, có máy bắn bóng, camera ghi hình, bảng điểm điện tử và huấn luyện viên theo lịch.',
 '4 sân chuyên nghiệp có camera ghi hình', 4, 0, 'Hà Nội',
 '32 Lê Đức Thọ, Phường Mỹ Đình 2, Quận Nam Từ Liêm, Hà Nội', 30000, 'ACTIVE', NULL),
(13, 'Đà Nẵng Ocean Pickleball', 120000,
 'https://www.meanwell.com/PBM/tw/img/portfolio/JR09-large.jpg',
 'Cụm sân gần biển, mặt sân chống ảnh hưởng của thời tiết, có mái che di động và khu vệ sinh, thay đồ sạch sẽ.',
 '4 sân gần biển với mái che di động', 4, 10, 'Đà Nẵng',
 '90 Võ Nguyên Giáp, Phường Phước Mỹ, Quận Sơn Trà, Đà Nẵng', 20000, 'ACTIVE', NULL),
(14, 'Han River Paddle Club', 135000,
 'https://images.unsplash.com/photo-1761644273884-83839f8f22e5?auto=format&fit=crop&w=1200&q=80',
 'Không gian thể thao ven sông Hàn với sân đạt kích thước chuẩn, đèn thi đấu và khu vực giao lưu cho câu lạc bộ.',
 '3 sân tiêu chuẩn ven sông Hàn', 3, 0, 'Đà Nẵng',
 '125 Trần Hưng Đạo, Phường An Hải Bắc, Quận Sơn Trà, Đà Nẵng', 25000, 'ACTIVE', NULL),
(15, 'Nha Trang Sun Court', 125000,
 'https://tse4.mm.bing.net/th/id/OIP.7-8xE1oG1StQZKRXX8wm_AHaFj?r=0&rs=1&pid=ImgDetMain&o=7&rm=3',
 'Sân ngoài trời nhiều cây xanh, mặt sân chống trượt, có khu tắm tráng và cho thuê đầy đủ dụng cụ pickleball.',
 '4 sân ngoài trời nhiều cây xanh', 4, 5, 'Khánh Hòa',
 '18 Phạm Văn Đồng, Phường Vĩnh Hải, TP. Nha Trang, Khánh Hòa', 25000, 'ACTIVE', NULL),
(16, 'Cần Thơ Mekong Pickleball', 105000,
 'https://images.pexels.com/photos/15390858/pexels-photo-15390858.jpeg?auto=compress&cs=tinysrgb&w=1200',
 'Câu lạc bộ cộng đồng với giá hợp lý, sân có đèn buổi tối, khu gửi xe và lớp nhập môn miễn phí cuối tuần.',
 '5 sân cộng đồng, phù hợp người mới', 5, 10, 'Cần Thơ',
 '55 Nguyễn Văn Cừ, Phường An Khánh, Quận Ninh Kiều, Cần Thơ', 20000, 'ACTIVE', NULL),
(17, 'Biên Hòa Green Paddle', 110000,
 'https://shopvnb.com/uploads/images/san-pickleball-win-club-3.jpg',
 'Cụm sân trong khuôn viên xanh, có mái che, thông gió tự nhiên và khu vực nghỉ riêng cho đội, nhóm đông người.',
 '4 sân có mái che trong khuôn viên xanh', 4, 0, 'Đồng Nai',
 '77 Võ Thị Sáu, Phường Thống Nhất, TP. Biên Hòa, Đồng Nai', 20000, 'ACTIVE', NULL),
(18, 'Bình Dương Champion Court', 130000,
 'https://static.wixstatic.com/media/8fb1d1_ec253450a6f64caa8ec710a2ba197d77~mv2.jpg/v1/fill/w_953,h_674,q_90/8fb1d1_ec253450a6f64caa8ec710a2ba197d77~mv2.jpg',
 'Trung tâm thể thao hiện đại với sân acrylic, khán đài nhỏ, phòng thay đồ và khu vực tổ chức giải nội bộ doanh nghiệp.',
 '6 sân hiện đại dành cho đội nhóm', 6, 5, 'Bình Dương',
 '40 Đại lộ Bình Dương, Phường Phú Hòa, TP. Thủ Dầu Một, Bình Dương', 25000, 'ACTIVE', NULL),
(19, 'Vũng Tàu Seaside Pickleball', 135000,
 'https://i.ytimg.com/vi/xuS_sWSunrg/maxresdefault.jpg',
 'Sân gần biển với lớp phủ chống lóa, hàng rào chắn gió và hệ thống đèn phù hợp chơi vào sáng sớm hoặc buổi tối.',
 '3 sân chống lóa gần biển', 3, 0, 'Bà Rịa - Vũng Tàu',
 '30 Thùy Vân, Phường 2, TP. Vũng Tàu, Bà Rịa - Vũng Tàu', 25000, 'ACTIVE', NULL),
(20, 'Huế Imperial Paddle', 100000,
 'https://m7sport.vn/wp-content/uploads/2024/11/san-pickleball-pika-ball-thuy-san-1-1729737854.webp',
 'Câu lạc bộ pickleball yên tĩnh, có mái che mưa, mặt sân mới và khu vực sinh hoạt chung cho hội viên.',
 '3 sân có mái che tại trung tâm Huế', 3, 10, 'Thừa Thiên Huế',
 '102 Lê Lợi, Phường Phú Hội, TP. Huế, Thừa Thiên Huế', 20000, 'ACTIVE', NULL),
(21, 'Hải Phòng Port City Pickleball', 115000,
 'https://hjtep.org/images/bg-hero-lg.jpg',
 'Cụm sân tiêu chuẩn với khu vực khởi động, tủ khóa, phòng tắm và quầy bán bóng, quấn cán cùng phụ kiện cơ bản.',
 '4 sân tiêu chuẩn tại Ngô Quyền', 4, 0, 'Hải Phòng',
 '66 Lạch Tray, Phường Lạch Tray, Quận Ngô Quyền, Hải Phòng', 20000, 'ACTIVE', NULL),
(22, 'Hạ Long Bay Paddle Club', 145000,
 'https://tse4.mm.bing.net/th/id/OIP.QulffDiLNUk32-obkz-M9wHaFi?r=0&w=1280&h=956&rs=1&pid=ImgDetMain&o=7&rm=3',
 'Câu lạc bộ có không gian mở, mặt sân cao cấp, khu ngắm cảnh và dịch vụ thuê dụng cụ cho khách du lịch.',
 '3 sân cao cấp với không gian mở', 3, 5, 'Quảng Ninh',
 '20 Trần Quốc Nghiễn, Phường Hồng Gai, TP. Hạ Long, Quảng Ninh', 30000, 'ACTIVE', NULL),
(23, 'Đà Lạt Highland Pickleball', 120000,
 'https://file.hstatic.net/1000341630/file/2._fbdb6266c13a494992e288fb00b255a5.jpg',
 'Sân trong nhà tránh mưa và sương, hệ thống đèn ấm, mặt sân đàn hồi và khu vực nghỉ có đồ uống nóng.',
 '4 sân trong nhà giữa khí hậu cao nguyên', 4, 0, 'Lâm Đồng',
 '45 Phù Đổng Thiên Vương, Phường 8, TP. Đà Lạt, Lâm Đồng', 25000, 'ACTIVE', NULL);

-- Khung giờ hoạt động khác nhau theo nhóm địa điểm.
INSERT INTO `court_time` (`court_id`, `time_id`)
SELECT p.`id`, t.`id`
FROM `products` p
JOIN `available_time` t
  ON (p.`id` BETWEEN 4 AND 8  AND t.`id` BETWEEN 1 AND 18)
  OR (p.`id` BETWEEN 9 AND 13 AND t.`id` BETWEEN 2 AND 18)
  OR (p.`id` BETWEEN 14 AND 18 AND t.`id` BETWEEN 1 AND 17)
  OR (p.`id` BETWEEN 19 AND 23 AND t.`id` BETWEEN 3 AND 18)
WHERE p.`id` BETWEEN 4 AND 23;

-- Tạo sân con theo products.quantity. Mỗi product dành sẵn một dải 6 id.
INSERT INTO `sub_courts` (`id`, `name`, `product_id`)
SELECT 13 + ((p.`id` - 4) * 6) + (n.`id` - 1),
       CONCAT('Sân số ', n.`id`),
       p.`id`
FROM `products` p
JOIN `available_time` n ON n.`id` BETWEEN 1 AND p.`quantity`
WHERE p.`id` BETWEEN 4 AND 23;

-- Mỗi sân con thừa hưởng toàn bộ khung giờ của địa điểm chứa nó.
INSERT INTO `subcourt_available_time` (`sub_court_id`, `available_time_id`)
SELECT sc.`id`, ct.`time_id`
FROM `sub_courts` sc
JOIN `court_time` ct ON ct.`court_id` = sc.`product_id`
WHERE sc.`product_id` BETWEEN 4 AND 23;
