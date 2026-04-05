package org.makery.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    private String email;

    @NotNull
    private String name;

    // 소셜 로그인 시 번호를 못 가져올 수 있으므로 NotNull 해제 권장
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Language language;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    private OAuthProvider oAuthProvider;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private SellerProfile sellerProfile;

    // --- 비즈니스 로직 ---

    // 1. 단순 이름 업데이트 (OAuth2UserCustomService용)
    public User update(String name) {
        this.name = name;
        return this;
    }

    // 2. 이름과 언어 업데이트 (UserService용)
    public User update(String name, Language language) {
        this.name = name;
        this.language = language;
        return this;
    }
}