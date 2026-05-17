package com.tissugest.entity;

import com.tissugest.entity.enums.FeatureCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plan_features", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"plan_id", "feature_code"})
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PlanFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature_code", nullable = false, columnDefinition = "feature_code")
    private FeatureCode featureCode;
}
