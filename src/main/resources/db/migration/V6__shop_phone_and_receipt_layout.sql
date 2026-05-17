-- Add phone field to shops
ALTER TABLE shops ADD COLUMN IF NOT EXISTS phone VARCHAR(20);

-- Update receipt template: logo top-left layout
UPDATE document_templates SET html_content = '<!DOCTYPE html>
<html><head><meta charset="UTF-8"/>
<style>
body{font-family:Arial,sans-serif;font-size:12px;margin:0;padding:20px;color:#333}
.header{display:flex;align-items:center;gap:12px;border-bottom:2px solid #2563eb;padding-bottom:15px;margin-bottom:20px}
.header .logo{flex-shrink:0}
.header .info{flex:1}
.header .info h1{color:#2563eb;margin:0;font-size:18px}
.header .info p{margin:2px 0;color:#666;font-size:11px}
.doc-info{display:flex;justify-content:space-between;margin-bottom:15px;font-size:11px}
table{width:100%;border-collapse:collapse;margin:15px 0}
th{background:#f1f5f9;padding:8px;text-align:left;font-size:11px;border-bottom:2px solid #e2e8f0}
td{padding:8px;border-bottom:1px solid #f1f5f9;font-size:11px}
.total-section{text-align:right;margin-top:15px}
.total-section .grand-total{font-size:16px;font-weight:bold;color:#2563eb}
.footer{text-align:center;margin-top:30px;padding-top:15px;border-top:1px solid #e2e8f0;font-size:10px;color:#999}
</style></head><body>
<div class="header">
  <div class="logo">{{logoImg}}</div>
  <div class="info">
    <h1>{{shopName}}</h1>
    <p>{{shopAddress}}</p>
    <p>Tél: {{shopPhone}}</p>
  </div>
</div>
<div class="doc-info">
  <div><strong>REÇU N°</strong> {{docNumber}}</div>
  <div><strong>Date:</strong> {{date}}</div>
  <div>{{clientName}}</div>
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
</body></html>'
WHERE doc_type = 'RECEIPT' AND shop_id IS NULL;

-- Update invoice template: logo top-left
UPDATE document_templates SET html_content = '<!DOCTYPE html>
<html><head><meta charset="UTF-8"/>
<style>
body{font-family:Arial,sans-serif;font-size:12px;margin:0;padding:30px;color:#333}
.header{display:flex;justify-content:space-between;align-items:flex-start;border-bottom:3px solid #2563eb;padding-bottom:20px;margin-bottom:25px}
.header .company{display:flex;align-items:center;gap:12px;flex:1}
.header .company .info h1{color:#2563eb;margin:0;font-size:20px}
.header .company .info p{margin:2px 0;color:#666;font-size:11px}
.header .invoice-info{text-align:right}
.header .invoice-info h2{color:#2563eb;margin:0;font-size:24px}
.parties{display:flex;justify-content:space-between;margin-bottom:25px}
.parties>div{flex:1;padding:15px;background:#f8fafc;border-radius:8px;margin:0 5px}
.parties h3{margin:0 0 8px;font-size:12px;color:#64748b}
table{width:100%;border-collapse:collapse;margin:20px 0}
th{background:#1e40af;color:white;padding:10px;text-align:left;font-size:11px}
td{padding:10px;border-bottom:1px solid #e2e8f0;font-size:11px}
.total-box{float:right;width:250px;margin-top:20px}
.total-box div{display:flex;justify-content:space-between;padding:5px 0;font-size:12px}
.total-box .grand{font-size:16px;font-weight:bold;color:#2563eb;border-top:2px solid #2563eb;padding-top:10px}
.footer{clear:both;text-align:center;margin-top:60px;padding-top:15px;border-top:1px solid #e2e8f0;font-size:10px;color:#999}
</style></head><body>
<div class="header">
  <div class="company">
    <div class="logo">{{logoImg}}</div>
    <div class="info"><h1>{{shopName}}</h1><p>{{shopAddress}}</p><p>Tél: {{shopPhone}}</p></div>
  </div>
  <div class="invoice-info"><h2>FACTURE</h2><p><strong>N°</strong> {{docNumber}}</p><p><strong>Date:</strong> {{date}}</p></div>
</div>
<div class="parties">
  <div><h3>ÉMETTEUR</h3><strong>{{shopName}}</strong><br/>{{shopAddress}}<br/>Tél: {{shopPhone}}</div>
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
</body></html>'
WHERE doc_type = 'INVOICE' AND shop_id IS NULL;
