package org.makery.websocket;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.makery.config.jwt.TokenProvider;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(CustomHandshakeInterceptor.class);
    private final TokenProvider tokenProvider;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest req = servletRequest.getServletRequest();
            HttpSession session = req.getSession(false);

            if (session != null) {
                attributes.put("sessionID", session.getId());
                logger.info("세션 ID: {}", session.getId());
            }

            // 1. 토큰 파싱 및 검증
            String token = req.getParameter("token");
            if (token != null && tokenProvider.validToken(token)) {
                Long tokenUserId = tokenProvider.getUserId(token);
                attributes.put("userId", tokenUserId);
                logger.info("웹소켓 토큰 인증 성공 - userId: {}", tokenUserId);
            } else {
                logger.warn("웹소켓 토큰 인증 실패 또는 토큰 누락");
                return false; // 인증 실패 시 웹소켓 연결 거부
            }

            String userName = req.getParameter("userName");
            if (userName != null && !userName.isBlank()) {
                attributes.put("userName", userName);
            } else {
                attributes.put("userName", "anonymous");
            }

            String roomNumber = req.getParameter("roomNumber");
            if (roomNumber != null) {
                try {
                    attributes.put("roomNumber", Long.parseLong(roomNumber));
                } catch (NumberFormatException e) {
                    logger.warn("roomNumber 파싱 실패: {}", roomNumber);
                }
            }
        }

        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception ex
    ) {
        // 후처리 없음
    }
}
