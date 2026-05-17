-- ==============================
-- MESSAGING PROVIDER ENUM
-- ==============================
DO $$ BEGIN
    CREATE TYPE messaging_provider AS ENUM ('NONE', 'ORANGE_SMS', 'WHATSAPP');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- ==============================
-- SHOP: add messaging config + logo
-- ==============================
ALTER TABLE shops ADD COLUMN IF NOT EXISTS messaging_provider messaging_provider NOT NULL DEFAULT 'NONE';
ALTER TABLE shops ADD COLUMN IF NOT EXISTS messaging_enabled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE shops ADD COLUMN IF NOT EXISTS messaging_api_key VARCHAR(500);
ALTER TABLE shops ADD COLUMN IF NOT EXISTS messaging_api_secret VARCHAR(500);
ALTER TABLE shops ADD COLUMN IF NOT EXISTS messaging_sender VARCHAR(100) DEFAULT 'TissuGest';
ALTER TABLE shops ADD COLUMN IF NOT EXISTS messaging_phone_id VARCHAR(100);
ALTER TABLE shops ADD COLUMN IF NOT EXISTS logo_url VARCHAR(500);
ALTER TABLE shops ADD COLUMN IF NOT EXISTS receipt_footer TEXT;
ALTER TABLE shops ADD COLUMN IF NOT EXISTS country_code VARCHAR(5) DEFAULT 'CI';

-- ==============================
-- MESSAGE LOGS
-- ==============================
CREATE TABLE IF NOT EXISTS message_logs (
    id              BIGSERIAL PRIMARY KEY,
    shop_id         BIGINT NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    recipient_phone VARCHAR(20) NOT NULL,
    recipient_name  VARCHAR(255),
    provider        messaging_provider NOT NULL,
    message_type    VARCHAR(50) NOT NULL,  -- SCHEDULE_REMINDER, PAYMENT_CONFIRM, etc.
    content         TEXT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'SENT',  -- SENT, FAILED, DELIVERED
    external_id     VARCHAR(255),  -- ID from provider (Orange/WhatsApp)
    error_message   TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_message_logs_shop_id ON message_logs(shop_id);
CREATE INDEX IF NOT EXISTS idx_message_logs_created_at ON message_logs(created_at);
