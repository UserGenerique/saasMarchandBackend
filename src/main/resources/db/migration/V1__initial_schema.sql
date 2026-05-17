-- =====================================================
-- TissuGest - Schema initial complet
-- Version: V1
-- Description: Toutes les entités du modèle de données
-- =====================================================

-- ==============================
-- TYPES ENUM
-- ==============================

CREATE TYPE user_role AS ENUM ('ADMIN', 'MERCHANT', 'SELLER');
CREATE TYPE subscription_status AS ENUM ('ACTIVE', 'EXPIRED', 'SUSPENDED', 'CANCELLED');
CREATE TYPE product_unit AS ENUM ('METER', 'PIECE', 'LOT', 'ROLL');
CREATE TYPE stock_movement_type AS ENUM ('IN', 'OUT', 'ADJUSTMENT');
CREATE TYPE stock_reference_type AS ENUM ('SALE', 'SUPPLY', 'MANUAL', 'CANCELLATION');
CREATE TYPE supply_status AS ENUM ('PAID', 'PARTIAL', 'UNPAID');
CREATE TYPE sale_type AS ENUM ('QUICK', 'WITH_CLIENT', 'CREDIT');
CREATE TYPE sale_status AS ENUM ('COMPLETED', 'CREDIT_OPEN', 'CREDIT_SETTLED', 'CANCELLED');
CREATE TYPE payment_schedule_status AS ENUM ('PENDING', 'PAID', 'OVERDUE');
CREATE TYPE schedule_reference_type AS ENUM ('SALE', 'SUPPLY');
CREATE TYPE invoice_type AS ENUM ('RECEIPT', 'INVOICE');
CREATE TYPE notification_type AS ENUM (
    'SCHEDULE_REMINDER',
    'LOW_STOCK',
    'SUBSCRIPTION_EXPIRING',
    'SUBSCRIPTION_SUSPENDED',
    'PAYMENT_RECEIVED'
);
CREATE TYPE staff_role AS ENUM ('SELLER');
CREATE TYPE feature_code AS ENUM (
    'SALES', 'STOCK', 'CLIENTS', 'CREDITS', 'SUPPLIERS',
    'REPORTS', 'INVOICES', 'NOTIFICATIONS_ADVANCED', 'EXPORT'
);

-- ==============================
-- 1. USERS
-- ==============================

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    phone           VARCHAR(20) NOT NULL UNIQUE,
    email           VARCHAR(255),
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    role            user_role NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_role ON users(role);

-- ==============================
-- 2. MERCHANTS
-- ==============================

CREATE TABLE merchants (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    business_name   VARCHAR(255) NOT NULL,
    phone           VARCHAR(20),
    address         TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_merchants_user_id ON merchants(user_id);

-- ==============================
-- 3. SHOPS
-- ==============================

CREATE TABLE shops (
    id              BIGSERIAL PRIMARY KEY,
    merchant_id     BIGINT NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    address         TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_shops_merchant_id ON shops(merchant_id);

-- ==============================
-- 4. SUBSCRIPTION PLANS
-- ==============================

CREATE TABLE subscription_plans (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    price_fcfa      BIGINT NOT NULL,
    duration_days   INTEGER NOT NULL DEFAULT 30,
    is_trial        BOOLEAN NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ==============================
-- 5. PLAN FEATURES
-- ==============================

CREATE TABLE plan_features (
    id              BIGSERIAL PRIMARY KEY,
    plan_id         BIGINT NOT NULL REFERENCES subscription_plans(id) ON DELETE CASCADE,
    feature_code    feature_code NOT NULL,
    UNIQUE(plan_id, feature_code)
);

CREATE INDEX idx_plan_features_plan_id ON plan_features(plan_id);

-- ==============================
-- 6. SUBSCRIPTIONS
-- ==============================

CREATE TABLE subscriptions (
    id                  BIGSERIAL PRIMARY KEY,
    merchant_id         BIGINT NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
    plan_id             BIGINT NOT NULL REFERENCES subscription_plans(id),
    start_date          DATE NOT NULL,
    end_date            DATE NOT NULL,
    grace_period_days   INTEGER NOT NULL DEFAULT 3,
    status              subscription_status NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subscriptions_merchant_id ON subscriptions(merchant_id);
CREATE INDEX idx_subscriptions_status ON subscriptions(status);
CREATE INDEX idx_subscriptions_end_date ON subscriptions(end_date);

-- ==============================
-- 7. SUBSCRIPTION PAYMENTS
-- ==============================

CREATE TABLE subscription_payments (
    id                  BIGSERIAL PRIMARY KEY,
    subscription_id     BIGINT NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
    amount_fcfa         BIGINT NOT NULL,
    payment_date        DATE NOT NULL,
    payment_method      VARCHAR(50),
    reference           VARCHAR(255),
    recorded_by         BIGINT REFERENCES users(id),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subscription_payments_sub_id ON subscription_payments(subscription_id);

-- ==============================
-- 8. CATEGORIES
-- ==============================

CREATE TABLE categories (
    id              BIGSERIAL PRIMARY KEY,
    shop_id         BIGINT NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    icon            VARCHAR(50),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_categories_shop_id ON categories(shop_id);

-- ==============================
-- 9. PRODUCTS
-- ==============================

CREATE TABLE products (
    id                  BIGSERIAL PRIMARY KEY,
    shop_id             BIGINT NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    category_id         BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    name                VARCHAR(255) NOT NULL,
    purchase_price      BIGINT NOT NULL DEFAULT 0,
    selling_price       BIGINT NOT NULL DEFAULT 0,
    unit                product_unit NOT NULL DEFAULT 'PIECE',
    photo_url           VARCHAR(500),
    low_stock_threshold INTEGER NOT NULL DEFAULT 5,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_shop_id ON products(shop_id);
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_name ON products(shop_id, name);

-- ==============================
-- 10. STOCK
-- ==============================

CREATE TABLE stock (
    id              BIGSERIAL PRIMARY KEY,
    product_id      BIGINT NOT NULL UNIQUE REFERENCES products(id) ON DELETE CASCADE,
    quantity        DECIMAL(15, 2) NOT NULL DEFAULT 0,
    last_updated    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_stock_product_id ON stock(product_id);

-- ==============================
-- 11. STOCK MOVEMENTS
-- ==============================

CREATE TABLE stock_movements (
    id              BIGSERIAL PRIMARY KEY,
    product_id      BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    shop_id         BIGINT NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    type            stock_movement_type NOT NULL,
    quantity        DECIMAL(15, 2) NOT NULL,
    reference_type  stock_reference_type NOT NULL,
    reference_id    BIGINT,
    note            TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_stock_movements_product_id ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_shop_id ON stock_movements(shop_id);
CREATE INDEX idx_stock_movements_reference ON stock_movements(reference_type, reference_id);

-- ==============================
-- 12. SUPPLIERS
-- ==============================

CREATE TABLE suppliers (
    id              BIGSERIAL PRIMARY KEY,
    shop_id         BIGINT NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    phone           VARCHAR(20),
    address         TEXT,
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_suppliers_shop_id ON suppliers(shop_id);

-- ==============================
-- 13. SUPPLIES (Approvisionnements)
-- ==============================

CREATE TABLE supplies (
    id              BIGSERIAL PRIMARY KEY,
    shop_id         BIGINT NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    supplier_id     BIGINT NOT NULL REFERENCES suppliers(id),
    total_amount    BIGINT NOT NULL DEFAULT 0,
    amount_paid     BIGINT NOT NULL DEFAULT 0,
    status          supply_status NOT NULL DEFAULT 'UNPAID',
    supply_date     DATE NOT NULL DEFAULT CURRENT_DATE,
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_supplies_shop_id ON supplies(shop_id);
CREATE INDEX idx_supplies_supplier_id ON supplies(supplier_id);
CREATE INDEX idx_supplies_status ON supplies(status);

-- ==============================
-- 14. SUPPLY LINES
-- ==============================

CREATE TABLE supply_lines (
    id              BIGSERIAL PRIMARY KEY,
    supply_id       BIGINT NOT NULL REFERENCES supplies(id) ON DELETE CASCADE,
    product_id      BIGINT NOT NULL REFERENCES products(id),
    quantity        DECIMAL(15, 2) NOT NULL,
    unit_price      BIGINT NOT NULL,
    line_total      BIGINT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_supply_lines_supply_id ON supply_lines(supply_id);

-- ==============================
-- 15. CLIENTS
-- ==============================

CREATE TABLE clients (
    id              BIGSERIAL PRIMARY KEY,
    shop_id         BIGINT NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    phone           VARCHAR(20),
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_clients_shop_id ON clients(shop_id);

-- ==============================
-- 16. SALES (Ventes)
-- ==============================

CREATE TABLE sales (
    id                  BIGSERIAL PRIMARY KEY,
    shop_id             BIGINT NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    client_id           BIGINT REFERENCES clients(id) ON DELETE SET NULL,
    sale_type           sale_type NOT NULL,
    total_amount        BIGINT NOT NULL DEFAULT 0,
    discount_amount     BIGINT NOT NULL DEFAULT 0,
    amount_paid         BIGINT NOT NULL DEFAULT 0,
    remaining_amount    BIGINT NOT NULL DEFAULT 0,
    status              sale_status NOT NULL DEFAULT 'COMPLETED',
    sale_date           DATE NOT NULL DEFAULT CURRENT_DATE,
    created_by          BIGINT REFERENCES users(id),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sales_shop_id ON sales(shop_id);
CREATE INDEX idx_sales_client_id ON sales(client_id);
CREATE INDEX idx_sales_status ON sales(status);
CREATE INDEX idx_sales_sale_date ON sales(sale_date);
CREATE INDEX idx_sales_created_by ON sales(created_by);

-- ==============================
-- 17. SALE LINES
-- ==============================

CREATE TABLE sale_lines (
    id              BIGSERIAL PRIMARY KEY,
    sale_id         BIGINT NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
    product_id      BIGINT NOT NULL REFERENCES products(id),
    quantity        DECIMAL(15, 2) NOT NULL,
    unit_price      BIGINT NOT NULL,
    discount        BIGINT NOT NULL DEFAULT 0,
    line_total      BIGINT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sale_lines_sale_id ON sale_lines(sale_id);

-- ==============================
-- 18. CLIENT PAYMENTS
-- ==============================

CREATE TABLE client_payments (
    id              BIGSERIAL PRIMARY KEY,
    sale_id         BIGINT NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
    client_id       BIGINT NOT NULL REFERENCES clients(id),
    amount          BIGINT NOT NULL,
    payment_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    note            TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_client_payments_sale_id ON client_payments(sale_id);
CREATE INDEX idx_client_payments_client_id ON client_payments(client_id);

-- ==============================
-- 19. SUPPLIER PAYMENTS
-- ==============================

CREATE TABLE supplier_payments (
    id              BIGSERIAL PRIMARY KEY,
    supply_id       BIGINT NOT NULL REFERENCES supplies(id) ON DELETE CASCADE,
    supplier_id     BIGINT NOT NULL REFERENCES suppliers(id),
    amount          BIGINT NOT NULL,
    payment_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    note            TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_supplier_payments_supply_id ON supplier_payments(supply_id);
CREATE INDEX idx_supplier_payments_supplier_id ON supplier_payments(supplier_id);

-- ==============================
-- 20. PAYMENT SCHEDULES (Échéances)
-- ==============================

CREATE TABLE payment_schedules (
    id              BIGSERIAL PRIMARY KEY,
    reference_type  schedule_reference_type NOT NULL,
    reference_id    BIGINT NOT NULL,
    amount_due      BIGINT NOT NULL,
    due_date        DATE NOT NULL,
    status          payment_schedule_status NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payment_schedules_reference ON payment_schedules(reference_type, reference_id);
CREATE INDEX idx_payment_schedules_status ON payment_schedules(status);
CREATE INDEX idx_payment_schedules_due_date ON payment_schedules(due_date);

-- ==============================
-- 21. INVOICES
-- ==============================

CREATE TABLE invoices (
    id              BIGSERIAL PRIMARY KEY,
    sale_id         BIGINT NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
    shop_id         BIGINT NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    invoice_number  VARCHAR(50) NOT NULL UNIQUE,
    type            invoice_type NOT NULL DEFAULT 'RECEIPT',
    pdf_url         VARCHAR(500),
    generated_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_invoices_sale_id ON invoices(sale_id);
CREATE INDEX idx_invoices_shop_id ON invoices(shop_id);

-- ==============================
-- 22. CURRENCY SETTINGS
-- ==============================

CREATE TABLE currency_settings (
    id                      BIGSERIAL PRIMARY KEY,
    shop_id                 BIGINT NOT NULL UNIQUE REFERENCES shops(id) ON DELETE CASCADE,
    is_enabled              BOOLEAN NOT NULL DEFAULT FALSE,
    local_currency_name     VARCHAR(50) DEFAULT '',
    conversion_rate         DECIMAL(10, 4) NOT NULL DEFAULT 1.0,
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ==============================
-- 23. NOTIFICATIONS
-- ==============================

CREATE TABLE notifications (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type            notification_type NOT NULL,
    title           VARCHAR(255) NOT NULL,
    body            TEXT,
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at         TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(user_id, is_read);

-- ==============================
-- 24. ACTIVITY LOG
-- ==============================

CREATE TABLE activity_logs (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    shop_id         BIGINT REFERENCES shops(id) ON DELETE SET NULL,
    action          VARCHAR(100) NOT NULL,
    entity_type     VARCHAR(50) NOT NULL,
    entity_id       BIGINT,
    details         JSONB,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_activity_logs_user_id ON activity_logs(user_id);
CREATE INDEX idx_activity_logs_shop_id ON activity_logs(shop_id);
CREATE INDEX idx_activity_logs_created_at ON activity_logs(created_at);

-- ==============================
-- 25. SHOP STAFF
-- ==============================

CREATE TABLE shop_staff (
    id              BIGSERIAL PRIMARY KEY,
    shop_id         BIGINT NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role            staff_role NOT NULL DEFAULT 'SELLER',
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(shop_id, user_id)
);

CREATE INDEX idx_shop_staff_shop_id ON shop_staff(shop_id);
CREATE INDEX idx_shop_staff_user_id ON shop_staff(user_id);

-- ==============================
-- SEED DATA: Plan d'essai gratuit
-- ==============================

INSERT INTO subscription_plans (name, description, price_fcfa, duration_days, is_trial, is_active)
VALUES ('Essai Gratuit', 'Plan d''essai gratuit pendant 14 jours avec toutes les fonctionnalités', 0, 14, TRUE, TRUE);

-- Plan Essentiel
INSERT INTO subscription_plans (name, description, price_fcfa, duration_days, is_trial, is_active)
VALUES ('Essentiel', 'Plan de base pour les petits commerçants', 15000, 30, FALSE, TRUE);

-- Plan Premium
INSERT INTO subscription_plans (name, description, price_fcfa, duration_days, is_trial, is_active)
VALUES ('Premium', 'Plan complet avec toutes les fonctionnalités', 25000, 30, FALSE, TRUE);

-- Features pour plan Essai (toutes les features)
INSERT INTO plan_features (plan_id, feature_code)
SELECT 1, f.code::feature_code FROM (
    VALUES ('SALES'), ('STOCK'), ('CLIENTS'), ('CREDITS'), ('SUPPLIERS'),
           ('REPORTS'), ('INVOICES'), ('NOTIFICATIONS_ADVANCED'), ('EXPORT')
) AS f(code);

-- Features pour plan Essentiel (features de base)
INSERT INTO plan_features (plan_id, feature_code)
SELECT 2, f.code::feature_code FROM (
    VALUES ('SALES'), ('STOCK'), ('CLIENTS'), ('CREDITS'), ('SUPPLIERS')
) AS f(code);

-- Features pour plan Premium (toutes les features)
INSERT INTO plan_features (plan_id, feature_code)
SELECT 3, f.code::feature_code FROM (
    VALUES ('SALES'), ('STOCK'), ('CLIENTS'), ('CREDITS'), ('SUPPLIERS'),
           ('REPORTS'), ('INVOICES'), ('NOTIFICATIONS_ADVANCED'), ('EXPORT')
) AS f(code);
