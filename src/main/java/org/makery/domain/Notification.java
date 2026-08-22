package org.makery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String title; // 예: "[새 주문 접수]", "[견적 완료]"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message; // 예: "[도시락 케이크] 인페인팅 작업이 완료되었습니다."

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotificationType type = NotificationType.SYSTEM;

    private Long targetId; // 주문 ID 또는 채팅방 번호 등 관련 식별자

    @Column(nullable = false)
    private boolean isRead; // 읽음 여부

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.type == null) {
            this.type = NotificationType.SYSTEM;
        }
        this.isRead = false; // 기본값은 안읽음
    }

    public void read() {
        this.isRead = true;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}