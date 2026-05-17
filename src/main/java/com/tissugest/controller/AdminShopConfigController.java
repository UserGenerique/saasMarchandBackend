package com.tissugest.controller;

import com.tissugest.entity.Shop;
import com.tissugest.exception.BusinessException;
import com.tissugest.repository.ShopRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/shops")
@RequiredArgsConstructor
public class AdminShopConfigController {

    private final ShopRepository shopRepository;

    /** Get shop branding config by merchant ID */
    @GetMapping("/config/{merchantId}")
    public ResponseEntity<ShopBrandingResponse> getConfig(@PathVariable Long merchantId) {
        Shop shop = shopRepository.findByMerchantIdAndIsActiveTrue(merchantId)
                .stream().findFirst()
                .orElseThrow(() -> BusinessException.notFound("Boutique", merchantId));

        return ResponseEntity.ok(ShopBrandingResponse.from(shop));
    }

    /** Update shop branding config */
    @PutMapping("/config/{merchantId}")
    public ResponseEntity<ShopBrandingResponse> updateConfig(
            @PathVariable Long merchantId,
            @RequestBody ShopBrandingRequest request) {

        Shop shop = shopRepository.findByMerchantIdAndIsActiveTrue(merchantId)
                .stream().findFirst()
                .orElseThrow(() -> BusinessException.notFound("Boutique", merchantId));

        if (request.name != null) shop.setName(request.name);
        if (request.address != null) shop.setAddress(request.address);
        if (request.phone != null) shop.setPhone(request.phone.isEmpty() ? null : request.phone);
        if (request.logoUrl != null) shop.setLogoUrl(request.logoUrl.isEmpty() ? null : request.logoUrl);
        if (request.receiptFooter != null) shop.setReceiptFooter(request.receiptFooter.isEmpty() ? null : request.receiptFooter);

        shop = shopRepository.save(shop);
        return ResponseEntity.ok(ShopBrandingResponse.from(shop));
    }

    @Data
    public static class ShopBrandingRequest {
        String name;
        String address;
        String phone;
        String logoUrl;
        String receiptFooter;
    }

    @Data
    public static class ShopBrandingResponse {
        Long shopId;
        String name;
        String address;
        String phone;
        String logoUrl;
        String receiptFooter;

        static ShopBrandingResponse from(Shop shop) {
            ShopBrandingResponse r = new ShopBrandingResponse();
            r.shopId = shop.getId();
            r.name = shop.getName();
            r.address = shop.getAddress();
            r.phone = shop.getPhone();
            r.logoUrl = shop.getLogoUrl();
            r.receiptFooter = shop.getReceiptFooter();
            return r;
        }
    }
}
