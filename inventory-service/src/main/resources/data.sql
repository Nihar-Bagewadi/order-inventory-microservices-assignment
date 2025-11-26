-- ===============================
-- Seed Data
-- ===============================

INSERT INTO product (name, description)
VALUES
('Paracetamol', 'Pain relief / fever reducer'),
('Vitamin C', 'Immunity booster tablets');

INSERT INTO product_batch (product_id, batch_number, quantity, expiry_date)
VALUES
(1, 'PCM-001', 100, '2026-01-10'),
(1, 'PCM-002', 50, '2025-06-05'),
(2, 'VTC-001', 200, '2026-11-20');
