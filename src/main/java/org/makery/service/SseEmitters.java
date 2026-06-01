package org.makery.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SseEmitters {

    // 유저 ID를 Key로 하여 각각의 실시간 SSE 통로를 보관하는 맵
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * 유저의 SSE 통로를 등록합니다.
     */
    public SseEmitter add(Long userId, SseEmitter emitter) {
        this.emitters.put(userId, emitter);

        // 연결이 만료되거나 에러가 나면 맵에서 자동으로 삭제되도록 콜백 등록
        emitter.onCompletion(() -> {
            log.info("SSE 연결 만료 (User ID: {})", userId);
            this.emitters.remove(userId);
        });
        emitter.onTimeout(() -> {
            log.warn("SSE 연결 타임아웃 발생 (User ID: {})", userId);
            this.emitters.remove(userId);
        });
        emitter.onError((e) -> {
            log.error("SSE 연결 에러 발생 (User ID: {})", userId, e);
            this.emitters.remove(userId);
        });

        return emitter;
    }

    /**
     * 특정 유저에게 실시간으로 알림 이벤트를 전송합니다.
     */
    public void sendToUser(Long userId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName) // 프론트엔드가 구독할 이벤트 이름 (예: "notification")
                        .data(data));    // 전송할 실제 데이터 (DTO 등)
                log.info("SSE 실시간 알림 전송 성공 (User ID: {})", userId);
            } catch (IOException e) {
                log.error("SSE 실시간 알림 전송 실패, 연결을 제거합니다. (User ID: {})", userId);
                emitters.remove(userId);
            }
        }
    }
}