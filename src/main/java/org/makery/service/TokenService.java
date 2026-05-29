package org.makery.service;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.makery.config.jwt.TokenProvider;
import org.makery.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class TokenService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    // 로그인 시 액세스 토큰 생성
    public String generateAccessToken(User user) {
        return tokenProvider.createAccessToken(user);
    }

    // 로그인 시 리프레시 토큰 생성
    public String generateRefreshToken(User user) {
        return tokenProvider.createRefreshToken(user);
    }

    public String createNewAccessToken(String refreshToken) {
        // 토큰 유효성 검사에 실패하면 예외 발생
        if(!tokenProvider.validToken(refreshToken)) {
            throw new IllegalArgumentException("Unexpected token");
        }

        Long userId = refreshTokenService.findByRefreshToken(refreshToken).getUserId();
        User user = userService.findById(userId);

        return tokenProvider.createAccessToken(user);
    }

    public void validateToken(String accessToken) {
        if (!tokenProvider.validToken(accessToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is expired or invalid.");
        }
    }

    public Long getUserId(String token) {
        try {
            return tokenProvider.getUserId(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token.", e);
        }
    }
}
