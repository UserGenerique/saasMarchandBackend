package com.tissugest.controller;

import com.tissugest.entity.enums.FeatureCode;
import com.tissugest.security.RequiresFeature;
import com.tissugest.service.ProductImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/products/import")
@RequiredArgsConstructor
public class ProductImportController {

    private final ProductImportService productImportService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequiresFeature(FeatureCode.EXPORT)
    public ResponseEntity<ProductImportService.ImportResult> importCsv(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(productImportService.importCsv(file));
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] content = productImportService.generateTemplate().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=modele_produits.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(content);
    }
}
