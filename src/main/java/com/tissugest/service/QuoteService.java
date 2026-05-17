package com.tissugest.service;

import com.tissugest.entity.*;
import com.tissugest.entity.enums.DocType;
import com.tissugest.entity.enums.QuoteStatus;
import com.tissugest.exception.BusinessException;
import com.tissugest.repository.ClientRepository;
import com.tissugest.repository.ProductRepository;
import com.tissugest.repository.QuoteRepository;
import com.tissugest.security.SecurityHelper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final SecurityHelper securityHelper;
    private final DocumentNumberService docNumberService;
    private final PdfService pdfService;

    public List<Quote> list() {
        Shop shop = securityHelper.getCurrentShop();
        return quoteRepository.findByShopIdOrderByCreatedAtDesc(shop.getId());
    }

    public Quote getById(Long id) {
        Shop shop = securityHelper.getCurrentShop();
        return quoteRepository.findByIdAndShopId(id, shop.getId())
                .orElseThrow(() -> BusinessException.notFound("Devis", id));
    }

    @Transactional
    public Quote create(QuoteRequest request) {
        Shop shop = securityHelper.getCurrentShop();
        DocType docType = request.isProforma ? DocType.PROFORMA : DocType.QUOTE;

        Client client = null;
        if (request.clientId != null) {
            client = clientRepository.findByIdAndShopId(request.clientId, shop.getId())
                    .orElseThrow(() -> BusinessException.notFound("Client", request.clientId));
        }

        String quoteNumber = docNumberService.nextNumber(shop.getId(), docType);

        List<QuoteLine> lines = new ArrayList<>();
        long total = 0;
        long totalDiscount = 0;

        for (QuoteLineRequest lineReq : request.lines) {
            String description = lineReq.description;
            if (lineReq.productId != null && (description == null || description.isEmpty())) {
                Product p = productRepository.findByIdAndShopId(lineReq.productId, shop.getId()).orElse(null);
                if (p != null) description = p.getName();
            }

            long lineTotal = (long) (lineReq.unitPrice * lineReq.quantity) - lineReq.discount;
            lineTotal = Math.max(0, lineTotal);

            QuoteLine line = QuoteLine.builder()
                    .product(lineReq.productId != null ? productRepository.findById(lineReq.productId).orElse(null) : null)
                    .description(description != null ? description : "Article")
                    .quantity(BigDecimal.valueOf(lineReq.quantity))
                    .unitPrice(lineReq.unitPrice)
                    .discount(lineReq.discount)
                    .lineTotal(lineTotal)
                    .build();
            lines.add(line);
            total += lineTotal;
            totalDiscount += lineReq.discount;
        }

        Quote quote = Quote.builder()
                .shop(shop)
                .client(client)
                .quoteNumber(quoteNumber)
                .docType(docType)
                .totalAmount(total)
                .discountAmount(totalDiscount)
                .notes(request.notes)
                .validUntil(request.validUntil != null ? request.validUntil : LocalDate.now().plusDays(30))
                .build();
        quote = quoteRepository.save(quote);

        for (QuoteLine line : lines) {
            line.setQuote(quote);
        }
        quote.setLines(lines);
        quote = quoteRepository.save(quote);

        log.info("Quote created: id={}, number={}, type={}, total={}", quote.getId(), quoteNumber, docType, total);
        return quote;
    }

    public byte[] generatePdf(Long quoteId) {
        Quote quote = getById(quoteId);
        return pdfService.generateQuoteDocument(quote);
    }

    // DTOs
    @Data
    public static class QuoteRequest {
        Long clientId;
        boolean isProforma = false;
        String notes;
        LocalDate validUntil;
        List<QuoteLineRequest> lines;
    }

    @Data
    public static class QuoteLineRequest {
        Long productId;
        String description;
        double quantity = 1;
        long unitPrice = 0;
        long discount = 0;
    }
}
