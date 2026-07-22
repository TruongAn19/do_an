ALTER TABLE racket
    ADD COLUMN target_quantity INT NULL,
    ADD COLUMN pending_retirement_quantity INT NOT NULL DEFAULT 0;

UPDATE racket
SET target_quantity = quantity;
