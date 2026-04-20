package org.makery.repository;

import org.makery.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // 해당 닉네임이 DB에 존재하는지 확인 (있으면 true, 없으면 false)
    boolean existsByNickname(String nickname);
}
