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

    // [수정] 판매자: @Query를 사용하여 복잡한 객체 탐색을 명확한 JPQL로 대체
    @Query("select o from Order o " +
            "join o.store s " +
            "join s.sellerProfile sp " +
            "where sp.user.id = :sellerId " +
            "order by o.createdAt desc")
    List<Order> findAllBySellerId(@Param("sellerId") Long sellerId);

    // 특정 매장의 오늘 날짜 픽업 주문 조회
    @Query("select o from Order o " +
            "join o.store s " +
            "join s.sellerProfile sp " +
            "where sp.user.id = :sellerId " +
            "and o.pickupDate >= :start " +
            "and o.pickupDate <= :end " +
            "order by o.pickupDate asc")
    List<Order> findAllBySellerIdAndPickupDateBetween(@Param("sellerId") Long sellerId,
                                                      @Param("start") java.time.LocalDateTime start,
                                                      @Param("end") java.time.LocalDateTime end);

    // 구매자의 오늘 날짜 픽업 주문 조회
    @Query("select o from Order o " +
            "where o.user.id = :userId " +
            "and o.pickupDate >= :start " +
            "and o.pickupDate <= :end " +
            "order by o.pickupDate asc")
    List<Order> findAllByUserIdAndPickupDateBetween(@Param("userId") Long userId,
                                                    @Param("start") java.time.LocalDateTime start,
                                                    @Param("end") java.time.LocalDateTime end);
}