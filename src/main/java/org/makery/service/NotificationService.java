package org.makery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.makery.domain.Notification;
import org.makery.domain.NotificationType;
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
     * 알림 등록 및 실시간 Push
     */
    @Transactional
    public NotificationResponse createNotification(User user, String title, String message, NotificationType type, Long targetId) {
        if (user == null) {
            log.warn("알림 대상 사용자가 존재하지 않아 생성을 건너뜁니다.");
            return null;
        }

        // 1. DB에 영구 저장
        Notification notification = notificationRepository.save(Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type != null ? type : NotificationType.SYSTEM)
                .targetId(targetId)
                .build());

        NotificationResponse response = NotificationResponse.from(notification);

        // 2. 🌟 SSE 실시간 Push
        try {
            sseEmitters.sendToUser(user.getId(), "notification", response);
        } catch (Exception e) {
            log.warn("SSE 알림 전송 실패 (User ID: {}): {}", user.getId(), e.getMessage());
        }

        return response;
    }

    @Transactional
    public void createNotification(User user, String message) {
        createNotification(user, "알림", message, NotificationType.SYSTEM, null);
    }

    /**
     * 알림 단건 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다. ID: " + notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("본인의 알림만 읽음 처리할 수 있습니다.");
        }

        notification.markAsRead();
    }

    /**
     * 유저의 모든 알림 일괄 읽음 처리
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findAllByUserIdAndIsReadFalse(userId);
        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
        }
    }

    /**
     * 안 읽은 알림 개수 조회
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * 마이페이지/알림함용 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public Slice<NotificationResponse> getMyNotifications(User user, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(NotificationResponse::from);
    }
}