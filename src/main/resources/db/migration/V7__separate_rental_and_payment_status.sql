ALTER TABLE rental_tool
    ADD COLUMN payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    ADD COLUMN daily_stock_reserved BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE rental_tool
SET payment_status = CASE
    WHEN refund_status = 'REFUNDED' THEN 'REFUNDED'
    WHEN status IN ('DEPOSITED', 'PAID', 'COMPLETED')
         OR refund_status = 'PENDING_REFUND' THEN 'PAID'
    ELSE 'UNPAID'
END;

UPDATE rental_tool
SET status = 'PENDING'
WHERE status IN ('DEPOSITED', 'PAID');
