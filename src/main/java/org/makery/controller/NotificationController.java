package org.makery.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.makery.domain.PrincipalDetails;
import org.makery.dto.NotificationResponse;
import org.makery.service.NotificationService;
import org.makery.service.SseEmitters;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SseEmitters sseEmitters;

    /**
     * 1. [실시간 연결] 프론트엔드 브라우저와 백엔드 간의 SSE 실시간 알림 통신선 수립
     * 미디어 타입 규격인 'text/event-stream'을 지정해야 브라우저가 끊지 않고 스트리밍으로 인식합니다.
     * 타임아웃은 실제 클라우드 서버 배포 환경을 고려하여 15분(900,000ms)으로 넉넉하게 잡았습니다.
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SseEmitter> subscribe(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        Long userId = principalDetails.user().getId();
        SseEmitter emitter = new SseEmitter(900000L); // 15분 연결 상태 유지

        // 🌟 [Nginx 및 브라우저 버퍼링 방어선] 최초 연결 시 바닐라(더미) 데이터를 하나 즉시 전송해야
        // 503 Gateway Timeout이나 데이터 누락 에러 없이 정상적으로 파이프라인이 유지됩니다.
        try {
            emitter.send(SseEmitter.event()
                    .name("connect") // 프론트엔드가 감지할 커스텀 이벤트명
                    .data("SSE 연결이 활성화되었습니다. [User ID: " + userId + "]"));
        } catch (IOException e) {
            log.error("최초 SSE handshake 신호 푸시 실패. User ID: {}", userId, e);
        }

        // 서버 메모리 맵(ConcurrentHashMap)에 활성화된 세션 에미터 등록
        sseEmitters.add(userId, emitter);

        return ResponseEntity.ok(emitter);
    }

    /**
     * 2. [무한 스크롤 내역 조회] 마이페이지/알림함 진입 시 과거에 수신한 알림 내역 페이징 조회
     * 호출 예시: GET /api/notifications?page=0&size=10
     * @PageableDefault 지정을 통해 파라미터가 유입되지 않아도 최신 생성순(createdAt DESC), 10개씩 페이징을 보장합니다.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Slice<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Slice<NotificationResponse> responses = notificationService.getMyNotifications(principalDetails.user(), pageable);
        return ResponseEntity.ok(responses);
    }

    /**
     * 3. [미확인 알림 수 조회]
     * GET /api/notifications/unread-count
     */
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<java.util.Map<String, Object>> getUnreadCount(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        long count = notificationService.getUnreadCount(principalDetails.user().getId());
        return ResponseEntity.ok(java.util.Map.of("unreadCount", count));
    }

    /**
     * 4. [단건 알림 읽음 처리]
     * PATCH /api/notifications/{id}/read
     */
    @org.springframework.web.bind.annotation.PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @org.springframework.web.bind.annotation.PathVariable Long id) {

        notificationService.markAsRead(id, principalDetails.user().getId());
        return ResponseEntity.ok().build();
    }

    /**
     * 5. [전체 알림 일괄 읽음 처리]
     * PATCH /api/notifications/read-all
     */
    @org.springframework.web.bind.annotation.PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        notificationService.markAllAsRead(principalDetails.user().getId());
        return ResponseEntity.ok().build();
    }
}