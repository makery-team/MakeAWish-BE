package org.makery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";
    private final RestTemplate restTemplate;

    /**
     * 비동기로 Expo Push 알림 발송
     * @param pushTokens 대상 기기 푸시 토큰 목록
     * @param title 알림 제목
     * @param body 알림 본문
     * @param data 추가 데이터 (orderId, type 등)
     */
    @Async
    public void sendPushAsync(List<String> pushTokens, String title, String body, Map<String, Object> data) {
        if (pushTokens == null || pushTokens.isEmpty()) {
            return;
        }

        try {
            List<Map<String, Object>> messages = new ArrayList<>();

            for (String token : pushTokens) {
                if (token == null || token.isBlank()) continue;

                Map<String, Object> message = new HashMap<>();
                message.put("to", token);
                message.put("sound", "default");
                message.put("title", title);
                message.put("body", body);
                message.put("priority", "high");
                message.put("channelId", "default");
                if (data != null && !data.isEmpty()) {
                    message.put("data", data);
                }
                messages.add(message);
            }

            if (messages.isEmpty()) return;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<List<Map<String, Object>>> entity = new HttpEntity<>(messages, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    EXPO_PUSH_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("📱 [Expo Push] 발송 성공 (대상: {}대, 상태코드: {}): {}", messages.size(), response.getStatusCode(), title);

        } catch (Exception e) {
            log.warn("📱 [Expo Push] 발송 중 예외 발생: {}", e.getMessage());
        }
    }
}
