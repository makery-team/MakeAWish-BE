package org.makery.service;

import lombok.RequiredArgsConstructor;
import org.makery.domain.RefreshToken;
import org.makery.repository.RefreshTokenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void save(Long userId, String refreshToken) {
        RefreshToken existing = refreshTokenRepository.findByUserId(userId).orElse(null);

        if (existing != null) {
            existing.update(refreshToken); // 기존 엔티티의 토큰 값만 변경
        } else {
            existing = new RefreshToken(userId, refreshToken);
        }

        refreshTokenRepository.save(existing);
    }

    public RefreshToken findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected token"));
    }

    public String getTokenByUserId(Long userId) {
        return refreshTokenRepository.findByUserId(userId)
                .map(RefreshToken::getRefreshToken)
                .orElse(null);
    }
    
    public void delete(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByRefreshToken(refreshTokenValue)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Refresh token not found."));

        refreshTokenRepository.delete(refreshToken);
    }
}