package org.makery.websocket;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
public class CustomHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(CustomHandshakeInterceptor.class);

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

            String userName = req.getParameter("userName");
            if (userName != null && !userName.isBlank()) {
                attributes.put("userName", userName);
            } else {
                attributes.put("userName", "anonymous");
            }

            String roomNumber = req.getParameter("roomNumber");
            String userId = req.getParameter("userId");

            if (roomNumber != null) {
                try {
                    attributes.put("roomNumber", Long.parseLong(roomNumber));
                } catch (NumberFormatException e) {
                    logger.warn("roomNumber 파싱 실패: {}", roomNumber);
                }
            }

            if (userId != null) {
                try {
                    attributes.put("userId", Long.parseLong(userId));
                } catch (NumberFormatException e) {
                    logger.warn("userId 파싱 실패: {}", userId);
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
