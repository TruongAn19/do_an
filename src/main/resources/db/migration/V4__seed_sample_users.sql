-- ============================================================
-- V4__seed_sample_users.sql
-- Tài khoản khách hàng mẫu phục vụ phát triển/demo.
--
-- Cả hai tài khoản dùng mật khẩu: DemoUser@2026
-- Mật khẩu trong database được lưu bằng BCrypt, không lưu dạng thô.
-- Không seed ADMIN hoặc STAFF. Admin vẫn chỉ được bootstrap qua biến môi trường.
-- ============================================================

INSERT INTO `user`
    (`email`, `password`, `full_name`, `address`, `phone`,
     `member_level`, `active`, `role_id`)
SELECT
    'demo.user1@pickleball.local',
    '$2a$10$V5zX9NM1fLtNcWJpizUrT.3ZlYllEB99VW90bLF/8iJZT2cGrSMDa',
    'Nguyễn Minh Anh',
    'TP.HCM',
    '0902000001',
    'NORMAL',
    b'1',
    r.`id`
FROM `roles` r
WHERE r.`name` = 'USER'
  AND NOT EXISTS (
      SELECT 1 FROM `user` u
      WHERE u.`email` = 'demo.user1@pickleball.local'
  );

INSERT INTO `user`
    (`email`, `password`, `full_name`, `address`, `phone`,
     `member_level`, `active`, `role_id`)
SELECT
    'demo.user2@pickleball.local',
    '$2a$10$V5zX9NM1fLtNcWJpizUrT.3ZlYllEB99VW90bLF/8iJZT2cGrSMDa',
    'Trần Hoàng Nam',
    'Hà Nội',
    '0902000002',
    'SILVER',
    b'1',
    r.`id`
FROM `roles` r
WHERE r.`name` = 'USER'
  AND NOT EXISTS (
      SELECT 1 FROM `user` u
      WHERE u.`email` = 'demo.user2@pickleball.local'
  );
