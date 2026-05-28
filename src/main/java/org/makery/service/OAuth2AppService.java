package org.makery.service;

import lombok.RequiredArgsConstructor;
import org.makery.config.jwt.TokenProvider;
import org.makery.domain.OAuthProvider;
import org.makery.domain.RefreshToken;
import org.makery.domain.User;
import org.makery.domain.UserRole;
import org.makery.repository.RefreshTokenRepository;
import org.makery.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class OAuth2AppService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);

    @Transactional
    public Map<String, String> processUserLoginOrRegister(String email, String name) {
        // 1. 기존 OAuth2UserCustomService에 있던 동일 비즈니스 로직 적용
        User user = userRepository.findByEmail(email)
                .map(entity -> entity.update(name))
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .name(name)
                        .userRole(UserRole.ROLE_GUEST)
                        .oAuthProvider(OAuthProvider.GOOGLE)
                        .build()));

        // 2. 자체 서비스용 Refresh Token 발행 및 데이터베이스 저장
        String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);
        saveRefreshToken(user.getId(), refreshToken);

        // 3. 자체 서비스용 Access Token 발행
        String accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);

        // 4. 앱 응답용 포맷 매핑
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return tokens;
    }

    private void saveRefreshToken(Long userId, String newRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .map(entity -> entity.update(newRefreshToken))
                .orElse(new RefreshToken(userId, newRefreshToken));
        refreshTokenRepository.save(refreshToken);
    }
}