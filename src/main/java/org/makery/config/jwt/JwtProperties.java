package org.makery.config.jwt;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(

        @NotBlank(message = "JWT issuer(발급자) 설정이 누락되었습니다.")
        String issuer,

        @NotBlank(message = "JWT secret-key(비밀키) 설정이 누락되었습니다.")
        String secretKey,

        @NotNull(message = "JWT Access Token 만료 시간 설정이 누락되었습니다.")
        @Min(value = 60000, message = "Access Token 만료 시간은 최소 1분(60000ms) 이상이어야 합니다.")
        Long accessExpiration,

        @NotNull(message = "JWT Refresh Token 만료 시간 설정이 누락되었습니다.")
        @Min(value = 86400000, message = "Refresh Token 만료 시간은 최소 1일(86400000ms) 이상이어야 합니다.")
        Long refreshExpiration
) {
}
