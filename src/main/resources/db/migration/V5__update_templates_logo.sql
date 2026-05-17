-- Update default templates to include {{logoImg}} variable in header

-- Receipt: add logo before shop name
UPDATE document_templates SET html_content = REPLACE(html_content, '<h1>{{shopName}}</h1>', '{{logoImg}}<h1>{{shopName}}</h1>') WHERE doc_type = 'RECEIPT' AND shop_id IS NULL;

-- Invoice: add logo before shop name in company section
UPDATE document_templates SET html_content = REPLACE(html_content, '<div class="company"><h1>{{shopName}}</h1>', '<div class="company">{{logoImg}}<h1>{{shopName}}</h1>') WHERE doc_type = 'INVOICE' AND shop_id IS NULL;

-- Quote: add logo before shop name
UPDATE document_templates SET html_content = REPLACE(html_content, '<div class="company"><h1>{{shopName}}</h1>', '<div class="company">{{logoImg}}<h1>{{shopName}}</h1>') WHERE doc_type = 'QUOTE' AND shop_id IS NULL;

-- Proforma: add logo before shop name
UPDATE document_templates SET html_content = REPLACE(html_content, '<div class="company"><h1>{{shopName}}</h1>', '<div class="company">{{logoImg}}<h1>{{shopName}}</h1>') WHERE doc_type = 'PROFORMA' AND shop_id IS NULL;
