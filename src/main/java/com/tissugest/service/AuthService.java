package com.tissugest.service;

import com.tissugest.dto.auth.*;
import com.tissugest.entity.*;
import com.tissugest.entity.enums.SubscriptionStatus;
import com.tissugest.entity.enums.UserRole;
import com.tissugest.exception.BusinessException;
import com.tissugest.repository.*;
import com.tissugest.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final ShopRepository shopRepository;
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Vérifier unicité du téléphone
        if (userRepository.existsByPhone(request.getPhone())) {
            throw BusinessException.conflict("Ce numéro de téléphone est déjà utilisé");
        }

        // Créer l'utilisateur
        User user = User.builder()
                .phone(request.getPhone())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(UserRole.MERCHANT)
                .build();
        user = userRepository.save(user);

        // Créer le profil commerçant
        Merchant merchant = Merchant.builder()
                .user(user)
                .businessName(request.getBusinessName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();
        merchant = merchantRepository.save(merchant);

        // Créer la boutique par défaut
        Shop shop = Shop.builder()
                .merchant(merchant)
                .name(request.getBusinessName())
                .build();
        shop = shopRepository.save(shop);

        // Attribuer le plan d'essai gratuit
        SubscriptionPlan trialPlan = planRepository.findByIsTrialTrueAndIsActiveTrue()
                .orElseThrow(() -> BusinessException.badRequest("Aucun plan d'essai disponible"));

        Subscription subscription = Subscription.builder()
                .merchant(merchant)
                .plan(trialPlan)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(trialPlan.getDurationDays()))
                .status(SubscriptionStatus.ACTIVE)
                .build();
        subscriptionRepository.save(subscription);

        log.info("Nouveau commerçant inscrit: {} ({})", merchant.getBusinessName(), user.getPhone());

        // Générer les tokens
        return buildAuthResponse(user, merchant, shop);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> BusinessException.unauthorized("Identifiants incorrects"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw BusinessException.unauthorized("Identifiants incorrects");
        }

        if (!user.getIsActive()) {
            throw BusinessException.accountSuspended();
        }

        Merchant merchant = merchantRepository.findByUserId(user.getId()).orElse(null);
        Shop shop = null;
        if (merchant != null) {
            var shops = shopRepository.findByMerchantIdAndIsActiveTrue(merchant.getId());
            shop = shops.isEmpty() ? null : shops.get(0);
        }

        log.info("Connexion réussie: {}", user.getPhone());

        return buildAuthResponse(user, merchant, shop);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(token)) {
            throw BusinessException.unauthorized("Refresh token invalide ou expiré");
        }

        String tokenType = jwtTokenProvider.getTokenType(token);
        if (!"refresh".equals(tokenType)) {
            throw BusinessException.unauthorized("Token invalide");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.unauthorized("Utilisateur non trouvé"));

        if (!user.getIsActive()) {
            throw BusinessException.accountSuspended();
        }

        Merchant merchant = merchantRepository.findByUserId(user.getId()).orElse(null);
        Shop shop = null;
        if (merchant != null) {
            var shops = shopRepository.findByMerchantIdAndIsActiveTrue(merchant.getId());
            shop = shops.isEmpty() ? null : shops.get(0);
        }

        return buildAuthResponse(user, merchant, shop);
    }

    private AuthResponse buildAuthResponse(User user, Merchant merchant, Shop shop) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getPhone(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId(), user.getPhone(), user.getRole().name());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .merchantId(merchant != null ? merchant.getId() : null)
                .shopId(shop != null ? shop.getId() : null)
                .build();
    }
}
