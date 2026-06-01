package org.makery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_inpainted_designs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AiInpaintedDesign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_portfolio_id")
    private Portfolio originPortfolio;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String inpaintingPrompt;

    @Column(nullable = false)
    private String beforeImageUrl;

    @Column // 완료 전까지는 주소가 없으므로 nullable 허용
    private String afterImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InpaintingStatus status; // ★ 진행 상태 필드 추가

    @Builder.Default
    private boolean isStoredInAlbum = true;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 웹훅을 통해 결과가 도착했을 때 업데이트하는 메서드
    public void updateComplete(String permanentUrl) {
        this.afterImageUrl = permanentUrl;
        this.status = InpaintingStatus.COMPLETED;
    }

    public void updateFailed() {
        this.status = InpaintingStatus.FAILED;
    }
}
