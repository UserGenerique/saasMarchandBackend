package com.tissugest.controller;

import com.tissugest.entity.Sale;
import com.tissugest.entity.Shop;
import com.tissugest.entity.enums.DocType;
import com.tissugest.entity.enums.FeatureCode;
import com.tissugest.exception.BusinessException;
import com.tissugest.repository.SaleRepository;
import com.tissugest.security.RequiresFeature;
import com.tissugest.security.SecurityHelper;
import com.tissugest.service.DocumentNumberService;
import com.tissugest.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final PdfService pdfService;
    private final SaleRepository saleRepository;
    private final SecurityHelper securityHelper;
    private final DocumentNumberService docNumberService;

    @GetMapping("/sales/{saleId}/receipt")
    @RequiresFeature(FeatureCode.SALES)
    public ResponseEntity<byte[]> receipt(@PathVariable Long saleId) {
        return generateSaleDoc(saleId, DocType.RECEIPT);
    }

    @GetMapping("/sales/{saleId}/invoice")
    @RequiresFeature(FeatureCode.INVOICES)
    public ResponseEntity<byte[]> invoice(@PathVariable Long saleId) {
        return generateSaleDoc(saleId, DocType.INVOICE);
    }

    private ResponseEntity<byte[]> generateSaleDoc(Long saleId, DocType docType) {
        byte[] pdf = pdfService.generateSaleDocumentById(saleId, docType);
        String filename = docType.name().toLowerCase() + "_" + saleId + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
