ALTER TABLE booking ADD COLUMN booking_type VARCHAR(20) DEFAULT 'ONE_TIME';
ALTER TABLE booking ADD COLUMN recurring_end_date DATE;
