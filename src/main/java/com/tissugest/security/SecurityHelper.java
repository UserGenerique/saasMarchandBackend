package com.tissugest.security;

import com.tissugest.entity.Merchant;
import com.tissugest.entity.Shop;
import com.tissugest.exception.BusinessException;
import com.tissugest.repository.MerchantRepository;
import com.tissugest.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityHelper {

    private final MerchantRepository merchantRepository;
    private final ShopRepository shopRepository;

    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Non authentifié");
        }
        return (Long) auth.getPrincipal();
    }

    public Merchant getCurrentMerchant() {
        Long userId = getCurrentUserId();
        return merchantRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("Commerçant", userId));
    }

    public Shop getCurrentShop() {
        Merchant merchant = getCurrentMerchant();
        var shops = shopRepository.findByMerchantIdAndIsActiveTrue(merchant.getId());
        if (shops.isEmpty()) {
            throw BusinessException.badRequest("Aucune boutique active trouvée");
        }
        return shops.get(0);
    }
}
