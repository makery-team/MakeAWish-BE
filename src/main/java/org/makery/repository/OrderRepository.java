package org.makery.repository;

import org.makery.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select o from Order o " +
            "join fetch o.store " +
            "join fetch o.items " +
            "where o.id = :orderId")
    Optional<Order> findDetailById(@Param("orderId") Long orderId);

    // 구매자: 본인이 주문한 목록 조회 (최신순)
    List<Order> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    // 판매자: 본인 매장에 들어온 주문 목록 조회 (최신순)
    List<Order> findAllByStoreSellerProfileUserIdOrderByCreatedAtDesc(Long sellerId);
}
