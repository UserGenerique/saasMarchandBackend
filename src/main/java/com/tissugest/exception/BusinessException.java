package com.tissugest.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public BusinessException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    // Common factory methods
    public static BusinessException notFound(String entity, Object id) {
        return new BusinessException("NOT_FOUND",
                entity + " non trouvé(e) avec l'identifiant " + id, HttpStatus.NOT_FOUND);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException("CONFLICT", message, HttpStatus.CONFLICT);
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException("BAD_REQUEST", message, HttpStatus.BAD_REQUEST);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED);
    }

    public static BusinessException subscriptionExpired() {
        return new BusinessException("SUBSCRIPTION_EXPIRED",
                "Votre abonnement a expiré. Veuillez contacter l'administrateur.", HttpStatus.FORBIDDEN);
    }

    public static BusinessException featureNotAvailable(String feature) {
        return new BusinessException("FEATURE_NOT_AVAILABLE",
                "La fonctionnalité " + feature + " n'est pas incluse dans votre plan.", HttpStatus.FORBIDDEN);
    }

    public static BusinessException accountSuspended() {
        return new BusinessException("ACCOUNT_SUSPENDED",
                "Votre compte a été suspendu. Veuillez contacter l'administrateur.", HttpStatus.FORBIDDEN);
    }
}
