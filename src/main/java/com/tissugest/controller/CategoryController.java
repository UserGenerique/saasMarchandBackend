package com.tissugest.controller;

import com.tissugest.dto.category.CategoryRequest;
import com.tissugest.dto.category.CategoryResponse;
import com.tissugest.entity.enums.FeatureCode;
import com.tissugest.security.RequiresFeature;
import com.tissugest.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @RequiresFeature(FeatureCode.STOCK)
    public ResponseEntity<List<CategoryResponse>> list() {
        return ResponseEntity.ok(categoryService.listByShop());
    }

    @PostMapping
    @RequiresFeature(FeatureCode.STOCK)
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return new ResponseEntity<>(categoryService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @RequiresFeature(FeatureCode.STOCK)
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequiresFeature(FeatureCode.STOCK)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
