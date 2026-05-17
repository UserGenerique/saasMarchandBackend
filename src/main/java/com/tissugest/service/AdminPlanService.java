package com.tissugest.service;

import com.tissugest.dto.admin.PlanRequest;
import com.tissugest.dto.admin.PlanResponse;
import com.tissugest.entity.PlanFeature;
import com.tissugest.entity.SubscriptionPlan;
import com.tissugest.entity.enums.FeatureCode;
import com.tissugest.exception.BusinessException;
import com.tissugest.repository.PlanFeatureRepository;
import com.tissugest.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminPlanService {

    private final SubscriptionPlanRepository planRepository;
    private final PlanFeatureRepository planFeatureRepository;

    public List<PlanResponse> listAll() {
        return planRepository.findAll().stream()
                .map(PlanResponse::from)
                .collect(Collectors.toList());
    }

    public PlanResponse getById(Long id) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Plan", id));
        return PlanResponse.from(plan);
    }

    @Transactional
    public PlanResponse create(PlanRequest request) {
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(request.getName())
                .description(request.getDescription())
                .priceFcfa(request.getPriceFcfa())
                .durationDays(request.getDurationDays())
                .isTrial(request.getIsTrial())
                .build();
        plan = planRepository.save(plan);

        if (request.getFeatures() != null) {
            syncFeatures(plan, request.getFeatures());
        }

        plan = planRepository.findById(plan.getId()).orElseThrow();
        log.info("Plan créé: {} ({})", plan.getName(), plan.getId());
        return PlanResponse.from(plan);
    }

    @Transactional
    public PlanResponse update(Long id, PlanRequest request) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Plan", id));
        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setPriceFcfa(request.getPriceFcfa());
        plan.setDurationDays(request.getDurationDays());
        plan.setIsTrial(request.getIsTrial());
        plan = planRepository.save(plan);

        if (request.getFeatures() != null) {
            syncFeatures(plan, request.getFeatures());
        }

        plan = planRepository.findById(plan.getId()).orElseThrow();
        log.info("Plan mis à jour: {} ({})", plan.getName(), plan.getId());
        return PlanResponse.from(plan);
    }

    @Transactional
    public PlanResponse updateFeatures(Long id, List<FeatureCode> features) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Plan", id));
        syncFeatures(plan, features);
        plan = planRepository.findById(plan.getId()).orElseThrow();
        return PlanResponse.from(plan);
    }

    private void syncFeatures(SubscriptionPlan plan, List<FeatureCode> features) {
        // Supprimer les anciennes features
        List<PlanFeature> existing = planFeatureRepository.findByPlanId(plan.getId());
        planFeatureRepository.deleteAll(existing);

        // Créer les nouvelles
        for (FeatureCode code : features) {
            PlanFeature pf = PlanFeature.builder()
                    .plan(plan)
                    .featureCode(code)
                    .build();
            planFeatureRepository.save(pf);
        }
    }
}
