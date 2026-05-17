package com.tissugest.controller;

import com.tissugest.entity.DocumentTemplate;
import com.tissugest.entity.enums.DocType;
import com.tissugest.repository.DocumentTemplateRepository;
import com.tissugest.repository.ShopRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/templates")
@RequiredArgsConstructor
public class AdminTemplateController {

    private final DocumentTemplateRepository templateRepository;
    private final ShopRepository shopRepository;

    /** List all default (global) templates */
    @GetMapping("/defaults")
    public ResponseEntity<List<DocumentTemplate>> listDefaults() {
        return ResponseEntity.ok(templateRepository.findByShopIdIsNull());
    }

    /** List templates for a specific shop */
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<DocumentTemplate>> listByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(templateRepository.findByShopId(shopId));
    }

    /** Get a specific template */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentTemplate> getById(@PathVariable Long id) {
        return templateRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Update template HTML content */
    @PutMapping("/{id}")
    public ResponseEntity<DocumentTemplate> update(@PathVariable Long id, @RequestBody TemplateUpdateRequest request) {
        DocumentTemplate template = templateRepository.findById(id).orElse(null);
        if (template == null) return ResponseEntity.notFound().build();

        if (request.name != null) template.setName(request.name);
        if (request.htmlContent != null) template.setHtmlContent(request.htmlContent);
        template = templateRepository.save(template);
        return ResponseEntity.ok(template);
    }

    /** Create a shop-specific template (override) */
    @PostMapping
    public ResponseEntity<DocumentTemplate> create(@RequestBody TemplateCreateRequest request) {
        DocumentTemplate template = DocumentTemplate.builder()
                .shop(request.shopId != null ? shopRepository.findById(request.shopId).orElse(null) : null)
                .docType(DocType.valueOf(request.docType))
                .name(request.name)
                .htmlContent(request.htmlContent)
                .isDefault(request.shopId == null)
                .build();
        template = templateRepository.save(template);
        return ResponseEntity.ok(template);
    }

    @Data
    public static class TemplateUpdateRequest {
        String name;
        String htmlContent;
    }

    @Data
    public static class TemplateCreateRequest {
        Long shopId;
        String docType;
        String name;
        String htmlContent;
    }
}
