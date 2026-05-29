package org.makery.service;

import lombok.RequiredArgsConstructor;
import org.makery.config.GoogleTokenVerifier;
import org.makery.config.jwt.TokenProvider;
import org.makery.domain.OAuthProvider;
import org.makery.domain.User;
import org.makery.domain.UserRole;
import org.makery.dto.AuthTokenResponse;
import org.makery.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    @Transactional
    public AuthTokenResponse socialLogin(String provider, String token) {
        String email;
        String name;
        String providerId;

        // 1. 플랫폼별 토큰 검증 및 정보 추출
        switch (provider.toLowerCase()) {
            case "google":
                var payload = googleTokenVerifier.verify(token);
                email = payload.getEmail();
                name = (String) payload.get("name");
                providerId = payload.getSubject(); // 구글 고유 ID
                break;

            // case "kakao":
            //     카카오 서버와 통신하여 유저 정보를 가져오는 로직 추가 예정
            //     break;

            default:
                throw new IllegalArgumentException("지원하지 않는 소셜 로그인 플랫폼입니다: " + provider);
        }

        // 2. 유저 조회 또는 신규 가입 (Upsert)
        User user = userRepository.findByEmail(email)
                .map(existingUser -> {
                    existingUser.update(name); // 이름 변경 감지 시 업데이트
                    return existingUser;
                })
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .name(name)
                                .oAuthProvider(OAuthProvider.valueOf(provider.toUpperCase()))
                                .userRole(UserRole.ROLE_USER)
                                .build()
                ));

        // 3. 서비스 전용 자체 JWT 발급
        String accessToken = tokenProvider.createAccessToken(user);
        String refreshToken = tokenProvider.createRefreshToken(user);

        // 4. 응답 반환
        return new AuthTokenResponse(accessToken, refreshToken, user.getName());
    }
}
