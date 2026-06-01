package org.makery.repository;

import org.makery.domain.Like;
import org.makery.domain.Portfolio;
import org.makery.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    // 1. 이미 찜했는지 여부 확인 (중복 방지용)
    boolean existsByUserAndPortfolio(User user, Portfolio portfolio);

    // 2. 찜 취소를 위해 특정 유저와 포트폴리오 조합으로 찜 데이터 찾기
    Optional<Like> findByUserAndPortfolio(User user, Portfolio portfolio);

    // 3. 내 찜 목록 조회를 위해 유저가 누른 모든 찜 데이터 가져오기
    List<Like> findAllByUser(User user);
}