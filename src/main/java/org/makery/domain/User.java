package org.makery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users") // user는 DB 예약어인 경우가 많아 users로 설정
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email; // 로그인 식별자 (소셜 이메일 등)

    private String nickname;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role; // BUYER(구매자), SELLER(사장님)

    private String provider; // KAKAO, GOOGLE 등

    /**
     * 사장님일 경우 소유한 매장 정보 (1:1)
     */
    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL)
    private Store store;

    // --- Enum 정의 (내부 클래스로 작성하거나 별도 파일로 분리 가능) ---
    public enum UserRole {
        BUYER, SELLER
    }
}