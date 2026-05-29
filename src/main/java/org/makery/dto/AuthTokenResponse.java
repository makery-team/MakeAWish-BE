package org.makery.dto;

public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        String name
) {}
