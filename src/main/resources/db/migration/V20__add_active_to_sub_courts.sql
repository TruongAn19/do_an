ALTER TABLE sub_courts
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE products p
SET quantity = (
    SELECT COUNT(*)
    FROM sub_courts sc
    WHERE sc.product_id = p.id
      AND sc.active = TRUE
);
