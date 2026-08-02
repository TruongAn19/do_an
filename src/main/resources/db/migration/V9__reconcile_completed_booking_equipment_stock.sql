-- Reconcile legacy ON_SITE rentals created before booking completion cascaded
-- to the rental status and inventory. Only rows that still explicitly own a
-- stock reservation are restored, making this migration safe and idempotent.

UPDATE equipment e
JOIN (
    SELECT rt.equipment_id, SUM(rt.quantity) AS quantity_to_restore
    FROM rental_tool rt
    JOIN booking b ON CAST(b.id AS CHAR) = rt.booking_id
    WHERE rt.type = 'ON_SITE'
      AND rt.on_site_stock_reserved = TRUE
      AND rt.status <> 'CANCELLED'
      AND b.status IN ('Đã thanh toán', 'DA_THANH_TOAN')
    GROUP BY rt.equipment_id
) stale ON stale.equipment_id = e.id
SET e.booking_stock_quantity = LEAST(
    e.quantity,
    e.booking_stock_quantity + stale.quantity_to_restore
);

UPDATE rental_tool rt
JOIN booking b ON CAST(b.id AS CHAR) = rt.booking_id
SET rt.status = 'COMPLETED',
    rt.payment_status = 'PAID',
    rt.on_site_stock_reserved = FALSE,
    rt.update_at = CURRENT_TIMESTAMP
WHERE rt.type = 'ON_SITE'
  AND rt.on_site_stock_reserved = TRUE
  AND rt.status <> 'CANCELLED'
  AND b.status IN ('Đã thanh toán', 'DA_THANH_TOAN');
