package org.makery.repository;

import org.makery.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    // [수정] 판매자: @Query를 사용하여 복잡한 객체 탐색을 명확한 JPQL로 대체
    @Query("select o from Order o " +
            "join o.store s " +
            "join s.sellerProfile sp " +
            "where sp.user.id = :sellerId " +
            "order by o.createdAt desc")
    List<Order> findAllBySellerId(@Param("sellerId") Long sellerId);

    // 판매자: 특정 날짜 범위 주문 목록 조회 (오늘 주문 조회용)
    @Query("select o from Order o " +
            "join o.store s " +
            "join s.sellerProfile sp " +
            "where sp.user.id = :sellerId " +
            "and o.createdAt >= :startOfDay and o.createdAt <= :endOfDay " +
            "order by o.createdAt desc")
    List<Order> findAllBySellerIdAndCreatedAtBetween(
            @Param("sellerId") Long sellerId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    // 구매자: 특정 날짜 범위 주문 목록 조회 (오늘 주문 조회용)
    @Query("select o from Order o " +
            "where o.user.id = :userId " +
            "and o.createdAt >= :startOfDay and o.createdAt <= :endOfDay " +
            "order by o.createdAt desc")
    List<Order> findAllByUserIdAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    /**
     * 주문 번호(orderNumber) 기반 주문 조회
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);
}