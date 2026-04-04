package org.makery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
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
    private String email;

    // ⬇️ 이 필드를 추가했습니다!
    @Column(nullable = false)
    private String password;

    private String nickname;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role; // BUYER(구매자), SELLER(사장님)

    private String provider; // KAKAO, GOOGLE, LOCAL 등

    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL)
    private Store store;

    public enum UserRole {
        BUYER, SELLER
    }
}
