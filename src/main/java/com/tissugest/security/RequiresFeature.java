package com.tissugest.security;

import com.tissugest.entity.enums.FeatureCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to declare that an endpoint requires a specific subscription feature.
 * Used in combination with SubscriptionAspect to automatically check feature access.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresFeature {
    FeatureCode value();
}
