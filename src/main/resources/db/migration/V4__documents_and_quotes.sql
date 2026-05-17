-- ==============================
-- DOCUMENT TYPE ENUM
-- ==============================
DO $$ BEGIN
    CREATE TYPE document_type AS ENUM ('RECEIPT', 'INVOICE', 'QUOTE', 'PROFORMA');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- ==============================
-- DOCUMENT TEMPLATES
-- ==============================
CREATE TABLE IF NOT EXISTS document_templates (
    id              BIGSERIAL PRIMARY KEY,
    shop_id         BIGINT REFERENCES shops(id) ON DELETE CASCADE,  -- NULL = default global template
    doc_type        document_type NOT NULL,
    name            VARCHAR(100) NOT NULL,
    html_content    TEXT NOT NULL,
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_doc_templates_shop ON document_templates(shop_id, doc_type);

-- ==============================
-- QUOTES (Devis / Proforma)
-- ==============================
DO $$ BEGIN
    CREATE TYPE quote_status AS ENUM ('DRAFT', 'SENT', 'ACCEPTED', 'REJECTED', 'CONVERTED', 'EXPIRED');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

CREATE TABLE IF NOT EXISTS quotes (
    id              BIGSERIAL PRIMARY KEY,
    shop_id         BIGINT NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    client_id       BIGINT REFERENCES clients(id) ON DELETE SET NULL,
    quote_number    VARCHAR(50) NOT NULL,
    doc_type        document_type NOT NULL DEFAULT 'QUOTE',
    status          quote_status NOT NULL DEFAULT 'DRAFT',
    total_amount    BIGINT NOT NULL DEFAULT 0,
    discount_amount BIGINT NOT NULL DEFAULT 0,
    notes           TEXT,
    valid_until     DATE,
    converted_sale_id BIGINT REFERENCES sales(id) ON DELETE SET NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS quote_lines (
    id              BIGSERIAL PRIMARY KEY,
    quote_id        BIGINT NOT NULL REFERENCES quotes(id) ON DELETE CASCADE,
    product_id      BIGINT REFERENCES products(id) ON DELETE SET NULL,
    description     VARCHAR(500) NOT NULL,
    quantity        DECIMAL(15, 2) NOT NULL DEFAULT 1,
    unit_price      BIGINT NOT NULL DEFAULT 0,
    discount        BIGINT NOT NULL DEFAULT 0,
    line_total      BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_quotes_shop ON quotes(shop_id);
CREATE INDEX IF NOT EXISTS idx_quote_lines_quote ON quote_lines(quote_id);

-- ==============================
-- DOCUMENT NUMBERING SEQUENCE
-- ==============================
CREATE TABLE IF NOT EXISTS document_sequences (
    id              BIGSERIAL PRIMARY KEY,
    shop_id         BIGINT NOT NULL REFERENCES shops(id) ON DELETE CASCADE,
    doc_type        document_type NOT NULL,
    prefix          VARCHAR(10) NOT NULL,
    current_number  INTEGER NOT NULL DEFAULT 0,
    UNIQUE(shop_id, doc_type)
);

-- ==============================
-- SEED: Default templates
-- ==============================

-- Receipt template
INSERT INTO document_templates (shop_id, doc_type, name, html_content, is_default) VALUES
(NULL, 'RECEIPT', 'Reçu standard', '<!DOCTYPE html>
<html><head><meta charset="UTF-8"/>
<style>
body{font-family:Arial,sans-serif;font-size:12px;margin:0;padding:20px;color:#333}
.header{text-align:center;border-bottom:2px solid #2563eb;padding-bottom:15px;margin-bottom:20px}
.header h1{color:#2563eb;margin:0;font-size:20px}
.header p{margin:3px 0;color:#666;font-size:11px}
.info-row{display:flex;justify-content:space-between;margin-bottom:15px;font-size:11px}
.info-row div{flex:1}
table{width:100%;border-collapse:collapse;margin:15px 0}
th{background:#f1f5f9;padding:8px;text-align:left;font-size:11px;border-bottom:2px solid #e2e8f0}
td{padding:8px;border-bottom:1px solid #f1f5f9;font-size:11px}
.total-section{text-align:right;margin-top:15px}
.total-section .grand-total{font-size:16px;font-weight:bold;color:#2563eb}
.footer{text-align:center;margin-top:30px;padding-top:15px;border-top:1px solid #e2e8f0;font-size:10px;color:#999}
</style></head><body>
<div class="header">
  <h1>{{shopName}}</h1>
  <p>{{shopAddress}}</p>
  <p>Tél: {{shopPhone}}</p>
</div>
<div class="info-row">
  <div><strong>REÇU N°</strong> {{docNumber}}</div>
  <div style="text-align:center"><strong>Date:</strong> {{date}}</div>
  <div style="text-align:right">{{clientName}}</div>
</div>
<table>
  <thead><tr><th>Article</th><th>Qté</th><th>P.U.</th><th>Total</th></tr></thead>
  <tbody>{{lines}}</tbody>
</table>
<div class="total-section">
  <p>Sous-total: {{subtotal}} FCFA</p>
  <p>Remise: {{discount}} FCFA</p>
  <p class="grand-total">TOTAL: {{total}} FCFA</p>
  <p>Payé: {{paid}} FCFA</p>
</div>
<div class="footer">{{receiptFooter}}<br/>Merci de votre confiance !</div>
</body></html>', TRUE),

-- Invoice template
(NULL, 'INVOICE', 'Facture standard', '<!DOCTYPE html>
<html><head><meta charset="UTF-8"/>
<style>
body{font-family:Arial,sans-serif;font-size:12px;margin:0;padding:30px;color:#333}
.header{display:flex;justify-content:space-between;border-bottom:3px solid #2563eb;padding-bottom:20px;margin-bottom:25px}
.header .company{flex:1}
.header .company h1{color:#2563eb;margin:0;font-size:22px}
.header .invoice-info{text-align:right}
.header .invoice-info h2{color:#2563eb;margin:0;font-size:28px}
.parties{display:flex;justify-content:space-between;margin-bottom:25px}
.parties>div{flex:1;padding:15px;background:#f8fafc;border-radius:8px;margin:0 5px}
.parties h3{margin:0 0 8px;font-size:12px;color:#64748b}
table{width:100%;border-collapse:collapse;margin:20px 0}
th{background:#1e40af;color:white;padding:10px;text-align:left;font-size:11px}
td{padding:10px;border-bottom:1px solid #e2e8f0;font-size:11px}
tr:nth-child(even){background:#f8fafc}
.total-box{float:right;width:250px;margin-top:20px}
.total-box div{display:flex;justify-content:space-between;padding:5px 0;font-size:12px}
.total-box .grand{font-size:16px;font-weight:bold;color:#2563eb;border-top:2px solid #2563eb;padding-top:10px;margin-top:5px}
.footer{clear:both;text-align:center;margin-top:60px;padding-top:15px;border-top:1px solid #e2e8f0;font-size:10px;color:#999}
</style></head><body>
<div class="header">
  <div class="company"><h1>{{shopName}}</h1><p>{{shopAddress}}</p><p>Tél: {{shopPhone}}</p></div>
  <div class="invoice-info"><h2>FACTURE</h2><p><strong>N°</strong> {{docNumber}}</p><p><strong>Date:</strong> {{date}}</p></div>
</div>
<div class="parties">
  <div><h3>ÉMETTEUR</h3><strong>{{shopName}}</strong><br/>{{shopAddress}}</div>
  <div><h3>CLIENT</h3><strong>{{clientName}}</strong><br/>{{clientPhone}}</div>
</div>
<table>
  <thead><tr><th>#</th><th>Description</th><th>Qté</th><th>P.U. (FCFA)</th><th>Total (FCFA)</th></tr></thead>
  <tbody>{{lines}}</tbody>
</table>
<div class="total-box">
  <div><span>Sous-total</span><span>{{subtotal}} FCFA</span></div>
  <div><span>Remise</span><span>{{discount}} FCFA</span></div>
  <div class="grand"><span>TOTAL</span><span>{{total}} FCFA</span></div>
</div>
<div class="footer">{{receiptFooter}}</div>
</body></html>', TRUE),

-- Quote template
(NULL, 'QUOTE', 'Devis standard', '<!DOCTYPE html>
<html><head><meta charset="UTF-8"/>
<style>
body{font-family:Arial,sans-serif;font-size:12px;margin:0;padding:30px;color:#333}
.header{display:flex;justify-content:space-between;border-bottom:3px solid #059669;padding-bottom:20px;margin-bottom:25px}
.header .company h1{color:#059669;margin:0;font-size:22px}
.header .doc-info{text-align:right}
.header .doc-info h2{color:#059669;margin:0}
.validity{background:#ecfdf5;padding:10px;border-radius:6px;margin-bottom:20px;font-size:11px;color:#065f46}
.parties{display:flex;justify-content:space-between;margin-bottom:25px}
.parties>div{flex:1;padding:15px;background:#f8fafc;border-radius:8px;margin:0 5px}
.parties h3{margin:0 0 8px;font-size:12px;color:#64748b}
table{width:100%;border-collapse:collapse;margin:20px 0}
th{background:#059669;color:white;padding:10px;text-align:left;font-size:11px}
td{padding:10px;border-bottom:1px solid #e2e8f0;font-size:11px}
.total-box{float:right;width:250px;margin-top:20px}
.total-box div{display:flex;justify-content:space-between;padding:5px 0;font-size:12px}
.total-box .grand{font-size:16px;font-weight:bold;color:#059669;border-top:2px solid #059669;padding-top:10px}
.footer{clear:both;text-align:center;margin-top:60px;padding-top:15px;border-top:1px solid #e2e8f0;font-size:10px;color:#999}
.notes{margin-top:30px;padding:15px;background:#fffbeb;border-radius:8px;font-size:11px}
</style></head><body>
<div class="header">
  <div class="company"><h1>{{shopName}}</h1><p>{{shopAddress}}</p><p>Tél: {{shopPhone}}</p></div>
  <div class="doc-info"><h2>{{docTitle}}</h2><p><strong>N°</strong> {{docNumber}}</p><p><strong>Date:</strong> {{date}}</p></div>
</div>
<div class="validity">Valable jusqu''au: {{validUntil}}</div>
<div class="parties">
  <div><h3>ÉMETTEUR</h3><strong>{{shopName}}</strong></div>
  <div><h3>CLIENT</h3><strong>{{clientName}}</strong><br/>{{clientPhone}}</div>
</div>
<table>
  <thead><tr><th>#</th><th>Description</th><th>Qté</th><th>P.U. (FCFA)</th><th>Total (FCFA)</th></tr></thead>
  <tbody>{{lines}}</tbody>
</table>
<div class="total-box">
  <div><span>Sous-total</span><span>{{subtotal}} FCFA</span></div>
  <div><span>Remise</span><span>{{discount}} FCFA</span></div>
  <div class="grand"><span>TOTAL</span><span>{{total}} FCFA</span></div>
</div>
<div class="notes"><strong>Notes:</strong> {{notes}}</div>
<div class="footer">{{receiptFooter}}</div>
</body></html>', TRUE),

-- Proforma (reuses quote template style)
(NULL, 'PROFORMA', 'Proforma standard', '<!DOCTYPE html>
<html><head><meta charset="UTF-8"/>
<style>
body{font-family:Arial,sans-serif;font-size:12px;margin:0;padding:30px;color:#333}
.header{display:flex;justify-content:space-between;border-bottom:3px solid #d97706;padding-bottom:20px;margin-bottom:25px}
.header .company h1{color:#d97706;margin:0;font-size:22px}
.header .doc-info{text-align:right}
.header .doc-info h2{color:#d97706;margin:0}
.parties{display:flex;justify-content:space-between;margin-bottom:25px}
.parties>div{flex:1;padding:15px;background:#f8fafc;border-radius:8px;margin:0 5px}
.parties h3{margin:0 0 8px;font-size:12px;color:#64748b}
table{width:100%;border-collapse:collapse;margin:20px 0}
th{background:#d97706;color:white;padding:10px;text-align:left;font-size:11px}
td{padding:10px;border-bottom:1px solid #e2e8f0;font-size:11px}
.total-box{float:right;width:250px;margin-top:20px}
.total-box div{display:flex;justify-content:space-between;padding:5px 0;font-size:12px}
.total-box .grand{font-size:16px;font-weight:bold;color:#d97706;border-top:2px solid #d97706;padding-top:10px}
.footer{clear:both;text-align:center;margin-top:60px;padding-top:15px;border-top:1px solid #e2e8f0;font-size:10px;color:#999}
</style></head><body>
<div class="header">
  <div class="company"><h1>{{shopName}}</h1><p>{{shopAddress}}</p><p>Tél: {{shopPhone}}</p></div>
  <div class="doc-info"><h2>FACTURE PROFORMA</h2><p><strong>N°</strong> {{docNumber}}</p><p><strong>Date:</strong> {{date}}</p></div>
</div>
<div class="parties">
  <div><h3>ÉMETTEUR</h3><strong>{{shopName}}</strong></div>
  <div><h3>CLIENT</h3><strong>{{clientName}}</strong><br/>{{clientPhone}}</div>
</div>
<table>
  <thead><tr><th>#</th><th>Description</th><th>Qté</th><th>P.U. (FCFA)</th><th>Total (FCFA)</th></tr></thead>
  <tbody>{{lines}}</tbody>
</table>
<div class="total-box">
  <div><span>Sous-total</span><span>{{subtotal}} FCFA</span></div>
  <div><span>Remise</span><span>{{discount}} FCFA</span></div>
  <div class="grand"><span>TOTAL</span><span>{{total}} FCFA</span></div>
</div>
<div class="footer">Ce document n''est pas une facture. {{receiptFooter}}</div>
</body></html>', TRUE);
