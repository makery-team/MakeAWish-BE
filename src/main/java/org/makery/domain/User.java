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

    private String password;

    @NotNull
    private String name; // 소셜 계정 이름

    @Column(unique = true)
    private String nickname; // 서비스 내 활동 닉네임

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Language language; // 주 사용 언어

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    private OAuthProvider oAuthProvider;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private SellerProfile sellerProfile; // 판매자 프로필

    /**
     * 소셜 계정의 최신 이름 정보를 동기화
     */
    public User update(String name) {
        this.name = name;
        return this;
    }

    /**
     * 소셜 가입 직후 필수 프로필 설정
     */
    public void updateProfile(String nickname, String phoneNumber, Language language) {
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.language = language;
    }

    /**
     * 사용자의 권한을 변경하는 비즈니스 메서드
     * (예: ROLE_GUEST -> ROLE_USER)
     */
    public void assignRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public void registerAsSeller(SellerProfile sellerProfile) {
        this.userRole = UserRole.ROLE_SELLER;
        this.sellerProfile = sellerProfile;
        sellerProfile.setUser(this); // 양방향 연관관계 설정 (SellerProfile에 setUser 필요)
    }
}
