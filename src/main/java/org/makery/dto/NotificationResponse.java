package org.makery.dto;

import lombok.Builder;
import org.makery.domain.Notification;
import org.makery.domain.NotificationType;
import java.time.LocalDateTime;

@Builder
public record NotificationResponse(
        Long id,
        String title,
        String message,
        NotificationType type,
        Long targetId,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle() != null ? notification.getTitle() : "알림")
                .message(notification.getMessage())
                .type(notification.getType() != null ? notification.getType() : NotificationType.SYSTEM)
                .targetId(notification.getTargetId())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}