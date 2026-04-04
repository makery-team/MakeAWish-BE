package org.makery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews") // 💡 DB 테이블 이름을 reviews로 지정
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 💡 JPA를 위한 기본 생성자
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 리뷰 내용

    @Column(nullable = false)
    private Integer rating; // 별점 (1~5)

    // 💡 연관관계 매핑 (누가 썼는지)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 💡 연관관계 매핑 (어떤 가게인지)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 💡 생성 시 시간을 자동으로 넣어주기 위한 로직
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}