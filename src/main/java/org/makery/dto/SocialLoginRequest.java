package org.makery.dto;

import jakarta.validation.constraints.NotBlank;

public record SocialLoginRequest(
        @NotBlank(message = "인증 토큰(idToken 또는 accessToken)은 필수입니다.")
        String token
) {}
