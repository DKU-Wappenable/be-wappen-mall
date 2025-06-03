package com.wappenable.be.global.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class TokenResponse {
    private final String accessToken;
    private final String refreshToken;

    // NOTE: 약관 동의 여부 추가
    private boolean termsAgreed; 
}
