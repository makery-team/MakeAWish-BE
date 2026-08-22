package org.makery.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_tokens", indexes = {
        @Index(name = "idx_device_token", columnList = "token", unique = true)
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DeviceToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 512)
    private String token; // Expo Push Token or FCM Token

    @Column(length = 32)
    private String platform; // ANDROID, IOS, WEB

    private LocalDateTime lastActiveAt;

    public void updateActiveTime() {
        this.lastActiveAt = LocalDateTime.now();
    }
}
