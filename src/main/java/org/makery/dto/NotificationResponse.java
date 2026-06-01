package org.makery.dto;

import lombok.Builder;
import org.makery.domain.Notification;
import java.time.LocalDateTime;

@Builder
public record NotificationResponse(
        Long id,
        String message,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}