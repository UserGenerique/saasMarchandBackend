package com.tissugest.repository;

import com.tissugest.entity.PlanFeature;
import com.tissugest.entity.enums.FeatureCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanFeatureRepository extends JpaRepository<PlanFeature, Long> {
    List<PlanFeature> findByPlanId(Long planId);
    boolean existsByPlanIdAndFeatureCode(Long planId, FeatureCode featureCode);
}
