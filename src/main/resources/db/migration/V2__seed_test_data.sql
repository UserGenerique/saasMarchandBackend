-- ==============================
-- ADMIN USER
-- password: admin123 (BCrypt hash)
-- ==============================
INSERT INTO users (phone, email, password_hash, full_name, role, is_active)
VALUES ('221700000000', 'admin@tissugest.com',
        '$2y$10$vPHC9I4py8oZBazQpqJoJeaQEWji2p4bZx5HdbmG1XlT3bFjy3wkq',
        'Admin TissuGest', 'ADMIN', TRUE);

-- ==============================
-- TEST MERCHANTS
-- password for all: test1234 (BCrypt hash)
-- ==============================

-- Merchant 1: Boutique Awa
INSERT INTO users (phone, email, password_hash, full_name, role, is_active)
VALUES ('221770001111', 'awa@test.com',
        '$2y$10$vPHC9I4py8oZBazQpqJoJeaQEWji2p4bZx5HdbmG1XlT3bFjy3wkq',
        'Awa Diallo', 'MERCHANT', TRUE);

INSERT INTO merchants (user_id, business_name, phone, address)
VALUES (currval('users_id_seq'), 'Boutique Awa Tissus', '221770001111', 'Marché Sandaga, Dakar');

INSERT INTO shops (merchant_id, name, is_active)
VALUES (currval('merchants_id_seq'), 'Boutique Awa Tissus', TRUE);

INSERT INTO subscriptions (merchant_id, plan_id, start_date, end_date, status)
VALUES (currval('merchants_id_seq'), 3, CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE + INTERVAL '20 days', 'ACTIVE');

INSERT INTO subscription_payments (subscription_id, amount_fcfa, payment_date, payment_method, reference)
VALUES (currval('subscriptions_id_seq'), 25000, CURRENT_DATE - INTERVAL '10 days', 'ORANGE_MONEY', 'OM-2025-001');

-- Merchant 2: Bazin Mamadou
INSERT INTO users (phone, email, password_hash, full_name, role, is_active)
VALUES ('221770002222', 'mamadou@test.com',
        '$2y$10$vPHC9I4py8oZBazQpqJoJeaQEWji2p4bZx5HdbmG1XlT3bFjy3wkq',
        'Mamadou Traoré', 'MERCHANT', TRUE);

INSERT INTO merchants (user_id, business_name, phone, address)
VALUES (currval('users_id_seq'), 'Bazin Royal Mamadou', '221770002222', 'Marché HLM, Dakar');

INSERT INTO shops (merchant_id, name, is_active)
VALUES (currval('merchants_id_seq'), 'Bazin Royal Mamadou', TRUE);

INSERT INTO subscriptions (merchant_id, plan_id, start_date, end_date, status)
VALUES (currval('merchants_id_seq'), 2, CURRENT_DATE - INTERVAL '25 days', CURRENT_DATE + INTERVAL '5 days', 'ACTIVE');

INSERT INTO subscription_payments (subscription_id, amount_fcfa, payment_date, payment_method, reference)
VALUES (currval('subscriptions_id_seq'), 15000, CURRENT_DATE - INTERVAL '25 days', 'WAVE', 'WV-2025-002');

-- Merchant 3: Fatou Couture (expired)
INSERT INTO users (phone, email, password_hash, full_name, role, is_active)
VALUES ('221770003333', 'fatou@test.com',
        '$2y$10$vPHC9I4py8oZBazQpqJoJeaQEWji2p4bZx5HdbmG1XlT3bFjy3wkq',
        'Fatou Ndiaye', 'MERCHANT', TRUE);

INSERT INTO merchants (user_id, business_name, phone, address)
VALUES (currval('users_id_seq'), 'Fatou Couture & Tissus', '221770003333', 'Marché Colobane, Dakar');

INSERT INTO shops (merchant_id, name, is_active)
VALUES (currval('merchants_id_seq'), 'Fatou Couture & Tissus', TRUE);

INSERT INTO subscriptions (merchant_id, plan_id, start_date, end_date, status)
VALUES (currval('merchants_id_seq'), 1, CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE - INTERVAL '16 days', 'EXPIRED');

-- Merchant 4: Ibrahim Textiles (suspended)
INSERT INTO users (phone, email, password_hash, full_name, role, is_active)
VALUES ('221770004444', null,
        '$2y$10$vPHC9I4py8oZBazQpqJoJeaQEWji2p4bZx5HdbmG1XlT3bFjy3wkq',
        'Ibrahim Keita', 'MERCHANT', FALSE);

INSERT INTO merchants (user_id, business_name, phone, address)
VALUES (currval('users_id_seq'), 'Ibrahim Textiles Premium', '221770004444', 'Avenue Blaise Diagne, Dakar');

INSERT INTO shops (merchant_id, name, is_active)
VALUES (currval('merchants_id_seq'), 'Ibrahim Textiles Premium', TRUE);

INSERT INTO subscriptions (merchant_id, plan_id, start_date, end_date, status)
VALUES (currval('merchants_id_seq'), 3, CURRENT_DATE - INTERVAL '15 days', CURRENT_DATE + INTERVAL '15 days', 'SUSPENDED');

INSERT INTO subscription_payments (subscription_id, amount_fcfa, payment_date, payment_method, reference)
VALUES (currval('subscriptions_id_seq'), 25000, CURRENT_DATE - INTERVAL '15 days', 'CASH', 'CASH-2025-003');

-- Merchant 5: Aminata Bazin (trial, expiring soon)
INSERT INTO users (phone, email, password_hash, full_name, role, is_active)
VALUES ('221770005555', 'aminata@test.com',
        '$2y$10$vPHC9I4py8oZBazQpqJoJeaQEWji2p4bZx5HdbmG1XlT3bFjy3wkq',
        'Aminata Coulibaly', 'MERCHANT', TRUE);

INSERT INTO merchants (user_id, business_name, phone, address)
VALUES (currval('users_id_seq'), 'Aminata Bazin Express', '221770005555', 'Marché Tilène, Dakar');

INSERT INTO shops (merchant_id, name, is_active)
VALUES (currval('merchants_id_seq'), 'Aminata Bazin Express', TRUE);

INSERT INTO subscriptions (merchant_id, plan_id, start_date, end_date, status)
VALUES (currval('merchants_id_seq'), 1, CURRENT_DATE - INTERVAL '11 days', CURRENT_DATE + INTERVAL '3 days', 'ACTIVE');
