package org.makery.config.oauth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.makery.repository.RefreshTokenRepository;
import org.makery.util.CookieUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OAuth2LogoutHandler implements LogoutHandler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // 1. 쿠키에서 리프레시 토큰 추출
        String refreshToken = CookieUtil.getCookie(request, "refresh_token")
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken != null) {
            // 2. DB에서 해당 리프레시 토큰 삭제
            refreshTokenRepository.deleteByRefreshToken(refreshToken);

            // 3. 브라우저 쿠키 삭제
            CookieUtil.deleteCookie(request, response, "refresh_token");
        }
    }
}
