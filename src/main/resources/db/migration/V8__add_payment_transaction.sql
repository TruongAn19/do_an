CREATE TABLE payment_transaction (
    id BIGINT NOT NULL AUTO_INCREMENT, txn_ref VARCHAR(32) NOT NULL,
    payment_type VARCHAR(32) NOT NULL, entity_id BIGINT NOT NULL,
    expected_amount BIGINT NOT NULL, order_info VARCHAR(255) NOT NULL,
    response_code VARCHAR(16), successful BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL, callback_at DATETIME, PRIMARY KEY (id),
    UNIQUE KEY uk_payment_transaction_txn_ref (txn_ref)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
