package org.makery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.makery.domain.Notification;
import org.makery.domain.User;
import org.makery.dto.NotificationResponse;
import org.makery.repository.NotificationRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SseEmitters sseEmitters; // 🌟 실시간 쏴주기용 매니저 주입

    /**
     * 알림 등록 및 실시간 Push (AI 웹훅 성공 시 호출됨)
     */
    @Transactional
    public void createNotification(User user, String message) {
        // 1. 추후 마이페이지 조회를 위해 DB에 영구 저장
        Notification notification = notificationRepository.save(Notification.builder()
                .user(user)
                .message(message)
                .build());

        NotificationResponse response = NotificationResponse.from(notification);

        // 2. 🌟 유저가 현재 로그인 상태(SSE 연결 상태)라면 실시간으로 알림 토스트 팝업 데이터 푸시!
        sseEmitters.sendToUser(user.getId(), "notification", response);
    }

    /**
     * 마이페이지용 기존 알림 조회 로직
     */
    @Transactional(readOnly = true)
    public Slice<NotificationResponse> getMyNotifications(User user, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(NotificationResponse::from);
    }
}