package com.tissugest.controller;

import com.tissugest.entity.Quote;
import com.tissugest.entity.enums.FeatureCode;
import com.tissugest.security.RequiresFeature;
import com.tissugest.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @GetMapping
    @RequiresFeature(FeatureCode.INVOICES)
    public ResponseEntity<List<Quote>> list() {
        return ResponseEntity.ok(quoteService.list());
    }

    @GetMapping("/{id}")
    @RequiresFeature(FeatureCode.INVOICES)
    public ResponseEntity<Quote> getById(@PathVariable Long id) {
        return ResponseEntity.ok(quoteService.getById(id));
    }

    @PostMapping
    @RequiresFeature(FeatureCode.INVOICES)
    public ResponseEntity<Quote> create(@Valid @RequestBody QuoteService.QuoteRequest request) {
        return new ResponseEntity<>(quoteService.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/pdf")
    @RequiresFeature(FeatureCode.INVOICES)
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        Quote quote = quoteService.getById(id);
        byte[] pdf = quoteService.generatePdf(id);
        String filename = quote.getDocType().name().toLowerCase() + "_" + quote.getQuoteNumber() + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
