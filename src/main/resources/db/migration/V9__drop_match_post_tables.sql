-- ============================================================
-- V9__drop_match_post_tables.sql
-- Xoá feature Match-Post (tìm đối thủ + chat real-time).
-- Thứ tự DROP: child (FK) → parent.
-- ============================================================

DROP TABLE IF EXISTS chat_messages;
DROP TABLE IF EXISTS match_participants;
DROP TABLE IF EXISTS match_posts;
