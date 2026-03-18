package org.makery.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "users") // user는 DB 예약어인 경우가 많아 users로 설정
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
    private String nickname;

    @NotNull
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Language language;

    @NotNull
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OAuthProvider provider;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private SellerProfile sellerProfile;

    @Builder
    public User(String email, String nickname, UserRole userRole) {
        this.email = email;
        this.nickname = nickname;
        this.userRole = userRole;
    }

    // --- 비즈니스 로직 ---

    public void updateProfile(String email, Language language) {
        this.email = email;
        this.language = language;
    }
}
