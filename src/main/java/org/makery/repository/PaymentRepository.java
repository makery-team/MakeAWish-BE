package org.makery.repository;

import org.makery.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * 주문 ID(orderId) 기반 결제 정보 조회
     */
    Optional<Payment> findByOrderId(Long orderId);

    // 주문 번호(orderNumber) 기반 결제 목록 조회
    List<Payment> findAllByOrderOrderNumber(String orderNumber);
}
