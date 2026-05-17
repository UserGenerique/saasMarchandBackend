package com.tissugest.service;

import com.tissugest.entity.*;
import com.tissugest.entity.enums.DocType;
import com.tissugest.exception.BusinessException;
import com.tissugest.repository.DocumentTemplateRepository;
import com.tissugest.repository.SaleRepository;
import com.tissugest.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xhtmlrenderer.pdf.ITextRenderer;

import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfService {

    private final DocumentTemplateRepository templateRepository;
    private final SaleRepository saleRepository;
    private final SecurityHelper securityHelper;
    private final DocumentNumberService docNumberService;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat NUM_FMT = NumberFormat.getInstance(Locale.FRANCE);

    @Value("${app.uploads.dir:./uploads}")
    private String uploadsDir;

    /**
     * Full transactional method: loads sale + shop + merchant, generates PDF.
     */
    @Transactional
    public byte[] generateSaleDocumentById(Long saleId, DocType docType) {
        Shop shop = securityHelper.getCurrentShop();
        Sale sale = saleRepository.findByIdAndShopId(saleId, shop.getId())
                .orElseThrow(() -> BusinessException.notFound("Vente", saleId));

        // Attach shop (fully loaded) and init lazy proxies
        sale.setShop(shop);
        sale.getLines().size();
        if (sale.getClient() != null) sale.getClient().getName();

        String docNumber = docNumberService.nextNumber(shop.getId(), docType);
        return generateSaleDocument(sale, docType, docNumber);
    }

    /**
     * Generate a PDF for a sale (receipt or invoice).
     */
    public byte[] generateSaleDocument(Sale sale, DocType docType, String docNumber) {
        Shop shop = sale.getShop();
        Map<String, String> vars = new HashMap<>();

        // Shop info
        vars.put("shopName", shop.getName());
        vars.put("shopAddress", shop.getAddress() != null ? shop.getAddress() : "");
        // Use shop phone first, fallback to merchant phone
        String phone = shop.getPhone() != null ? shop.getPhone() : (shop.getMerchant() != null ? shop.getMerchant().getPhone() : "");
        vars.put("shopPhone", phone != null ? phone : "");
        vars.put("receiptFooter", shop.getReceiptFooter() != null ? shop.getReceiptFooter() : "");
        vars.put("logoImg", buildLogoImg(shop.getLogoUrl()));

        // Document info
        vars.put("docNumber", docNumber);
        vars.put("date", sale.getSaleDate().format(DATE_FMT));
        vars.put("docTitle", docType == DocType.RECEIPT ? "REÇU" : "FACTURE");

        // Client info
        vars.put("clientName", sale.getClient() != null ? sale.getClient().getName() : "Client comptoir");
        vars.put("clientPhone", sale.getClient() != null && sale.getClient().getPhone() != null ? sale.getClient().getPhone() : "");

        // Lines
        StringBuilder linesHtml = new StringBuilder();
        int idx = 1;
        for (SaleLine line : sale.getLines()) {
            linesHtml.append("<tr>")
                    .append("<td>").append(idx++).append("</td>")
                    .append("<td>").append(line.getProduct().getName()).append("</td>")
                    .append("<td>").append(line.getQuantity().toPlainString()).append("</td>")
                    .append("<td>").append(formatAmount(line.getUnitPrice())).append("</td>")
                    .append("<td>").append(formatAmount(line.getLineTotal())).append("</td>")
                    .append("</tr>");
        }
        vars.put("lines", linesHtml.toString());

        // Totals
        vars.put("subtotal", formatAmount(sale.getTotalAmount() + sale.getDiscountAmount()));
        vars.put("discount", formatAmount(sale.getDiscountAmount()));
        vars.put("total", formatAmount(sale.getTotalAmount()));
        vars.put("paid", formatAmount(sale.getAmountPaid()));

        return renderPdf(shop.getId(), docType, vars);
    }

    /**
     * Generate a PDF for a quote or proforma.
     */
    public byte[] generateQuoteDocument(Quote quote) {
        Shop shop = quote.getShop();
        DocType docType = quote.getDocType();
        Map<String, String> vars = new HashMap<>();

        vars.put("shopName", shop.getName());
        vars.put("shopAddress", shop.getAddress() != null ? shop.getAddress() : "");
        vars.put("shopPhone", shop.getMerchant() != null ? shop.getMerchant().getPhone() : "");
        vars.put("receiptFooter", shop.getReceiptFooter() != null ? shop.getReceiptFooter() : "");

        vars.put("docNumber", quote.getQuoteNumber());
        vars.put("date", quote.getCreatedAt().format(DATE_FMT));
        vars.put("docTitle", docType == DocType.PROFORMA ? "FACTURE PROFORMA" : "DEVIS");
        vars.put("validUntil", quote.getValidUntil() != null ? quote.getValidUntil().format(DATE_FMT) : "—");
        vars.put("notes", quote.getNotes() != null ? quote.getNotes() : "");

        vars.put("clientName", quote.getClient() != null ? quote.getClient().getName() : "—");
        vars.put("clientPhone", quote.getClient() != null && quote.getClient().getPhone() != null ? quote.getClient().getPhone() : "");

        StringBuilder linesHtml = new StringBuilder();
        int idx = 1;
        for (QuoteLine line : quote.getLines()) {
            linesHtml.append("<tr>")
                    .append("<td>").append(idx++).append("</td>")
                    .append("<td>").append(line.getDescription()).append("</td>")
                    .append("<td>").append(line.getQuantity().toPlainString()).append("</td>")
                    .append("<td>").append(formatAmount(line.getUnitPrice())).append("</td>")
                    .append("<td>").append(formatAmount(line.getLineTotal())).append("</td>")
                    .append("</tr>");
        }
        vars.put("lines", linesHtml.toString());

        vars.put("subtotal", formatAmount(quote.getTotalAmount() + quote.getDiscountAmount()));
        vars.put("discount", formatAmount(quote.getDiscountAmount()));
        vars.put("total", formatAmount(quote.getTotalAmount()));

        return renderPdf(shop.getId(), docType, vars);
    }

    private byte[] renderPdf(Long shopId, DocType docType, Map<String, String> variables) {
        // Load template: shop-specific first, fallback to global default
        DocumentTemplate template = templateRepository.findBestTemplate(shopId, docType)
                .orElseThrow(() -> BusinessException.badRequest("Aucun template trouvé pour " + docType));

        // Replace variables
        String html = template.getHtmlContent();
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            html = html.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }

        // Render HTML → PDF
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(os);
            log.info("PDF generated: type={}, shop={}, size={}KB", docType, shopId, os.size() / 1024);
            return os.toByteArray();
        } catch (Exception e) {
            log.error("Erreur génération PDF", e);
            throw BusinessException.badRequest("Erreur génération PDF: " + e.getMessage());
        }
    }

    private String formatAmount(long amount) {
        return NUM_FMT.format(amount);
    }

    /** Convert logo file to base64 data URI for embedding in PDF */
    private String buildLogoImg(String logoUrl) {
        if (logoUrl == null || logoUrl.isEmpty()) return "";
        try {
            // Extract filename from URL like /uploads/files/abc.png
            String filename = logoUrl;
            if (filename.contains("/")) filename = filename.substring(filename.lastIndexOf("/") + 1);

            Path filePath = Paths.get(uploadsDir).toAbsolutePath().resolve(filename);
            if (!Files.exists(filePath)) {
                log.warn("Logo file not found: {}", filePath);
                return "";
            }

            byte[] data = Files.readAllBytes(filePath);
            String mimeType = Files.probeContentType(filePath);
            if (mimeType == null) mimeType = "image/png";
            String base64 = Base64.getEncoder().encodeToString(data);

            return "<img src=\"data:" + mimeType + ";base64," + base64 + "\" width=\"120\" height=\"40\" />";
        } catch (Exception e) {
            log.warn("Cannot load logo: {}", e.getMessage());
            return "";
        }
    }
}
