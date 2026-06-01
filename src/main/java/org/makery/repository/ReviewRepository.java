package org.makery.repository;

import org.makery.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 1. 기존 매장별 리뷰 목록 조회 (일반 페이징)
     * N+1 문제를 방지하고 한 번에 작성자(User) 정보까지 가져오기 위해 EntityGraph를 활용합니다.
     */
    @EntityGraph(attributePaths = {"store", "order", "order.user"})
    Page<Review> findByStoreId(Long storeId, Pageable pageable);

    /**
     * 2. [신규] 특정 주문 건에 이미 작성된 리뷰가 있는지 확인 (1:1 중복 검증)
     * 한 주문당 하나의 리뷰만 남길 수 있도록 방어할 때 사용됩니다.
     */
    boolean existsByOrderId(Long orderId);

    /**
     * 3. [신규] 마이페이지용: 내가 작성한 모든 리뷰 목록 조회 (무한 스크롤)
     * Review -> Order -> User 연관 관계 그래프를 타고 들어가 특정 유저의 ID로 데이터를 쪼개어 가져옵니다.
     * 데이터 로딩 최적화를 위해 필요한 연관 엔티티들을 한 번에 묶어서(Fetch) 쿼리합니다.
     */
    @EntityGraph(attributePaths = {"store", "order", "order.user"})
    Slice<Review> findByOrderUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}