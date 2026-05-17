package com.tissugest.service;

import com.tissugest.dto.category.CategoryRequest;
import com.tissugest.dto.category.CategoryResponse;
import com.tissugest.entity.Category;
import com.tissugest.entity.Shop;
import com.tissugest.exception.BusinessException;
import com.tissugest.repository.CategoryRepository;
import com.tissugest.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SecurityHelper securityHelper;

    public List<CategoryResponse> listByShop() {
        Shop shop = securityHelper.getCurrentShop();
        return categoryRepository.findByShopId(shop.getId()).stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        Shop shop = securityHelper.getCurrentShop();
        Category category = Category.builder()
                .shop(shop)
                .name(request.getName())
                .icon(request.getIcon())
                .build();
        category = categoryRepository.save(category);
        return CategoryResponse.from(category);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Shop shop = securityHelper.getCurrentShop();
        Category category = categoryRepository.findByIdAndShopId(id, shop.getId())
                .orElseThrow(() -> BusinessException.notFound("Catégorie", id));
        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category = categoryRepository.save(category);
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long id) {
        Shop shop = securityHelper.getCurrentShop();
        Category category = categoryRepository.findByIdAndShopId(id, shop.getId())
                .orElseThrow(() -> BusinessException.notFound("Catégorie", id));
        categoryRepository.delete(category);
    }
}
