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
    private Portfolio originPortfolio; // 인페인팅의 모태가 된 원본 디자인

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_message_id")
    private AiAgentMessage sourceMessage; // 인페인팅을 발생시킨 대화 맥락

    @Column(nullable = false, columnDefinition = "TEXT")
    private String inpaintingPrompt; // 무엇을 '인페인팅' 했는지 기록

    @Column(nullable = false)
    private String beforeImageUrl; // 인페인팅 전 (Original)

    @Column(nullable = false)
    private String afterImageUrl;  // 인페인팅 후 (S3에 저장된 최종본)

    @Builder.Default
    private boolean isStoredInAlbum = true;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}