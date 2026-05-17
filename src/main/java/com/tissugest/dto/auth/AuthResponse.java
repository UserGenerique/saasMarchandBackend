package com.tissugest.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String fullName;
    private String phone;
    private String role;
    private Long merchantId;
    private Long shopId;
}
