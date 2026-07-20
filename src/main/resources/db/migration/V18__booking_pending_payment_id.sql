ALTER TABLE booking
    ADD COLUMN pending_payment_id BIGINT NULL,
    ADD CONSTRAINT uk_booking_pending_payment_id UNIQUE (pending_payment_id);
