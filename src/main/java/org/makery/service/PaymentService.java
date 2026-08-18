package org.makery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.makery.client.TossPaymentsClient;
import org.makery.domain.Order;
import org.makery.domain.OrderStatus;
import org.makery.domain.Payment;
import org.makery.dto.PaymentResponse;
import org.makery.dto.TossPaymentConfirmRequest;
import org.makery.repository.OrderRepository;
import org.makery.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final TossPaymentsClient tossPaymentsClient;

    @Value("${toss.payments.secret-key}")
    private String secretKey;

    @Transactional
    public void confirmTossPayment(TossPaymentConfirmRequest request) {
        Order order = orderRepository.findByOrderNumber(request.orderNumber())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문 번호입니다: " + request.orderNumber()));

        // 위변조 검증 (DB 저장 금액 != 승인 요청 금액)
        if (order.getTotalPrice() != request.amount()) {
            throw new IllegalStateException("결제 금액 불일치! DB: " + order.getTotalPrice() + ", 요청: " + request.amount());
        }

        // 테스트 바이패스 키 처리 (개발/테스트 샌드박스 편의성)
        if (request.paymentKey() != null && request.paymentKey().startsWith("test_bypass_")) {
            log.info("🧪 [Payment] 테스트 바이패스 결제 승인: orderNumber={}", request.orderNumber());
            Payment payment = paymentRepository.findByOrderId(order.getId())
                    .orElseGet(() -> Payment.builder().order(order).amount(request.amount()).build());

            payment.completePayment(request.paymentKey());
            paymentRepository.save(payment);
            order.updateStatus(OrderStatus.PAID);
            return;
        }

        // Basic Auth 인코딩 (SecretKey + ":")
        String authHeader = "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        log.info("🔍 [Toss Payment] 생성된 Authorization Header: [{}]", authHeader);

        Map<String, Object> body = Map.of(
                "paymentKey", request.paymentKey(),
                "orderId", request.orderNumber(),
                "amount", request.amount()
        );

        try {
            tossPaymentsClient.confirmPayment(authHeader, body);

            Payment payment = paymentRepository.findByOrderId(order.getId())
                    .orElseGet(() -> Payment.builder().order(order).amount(request.amount()).build());

            payment.completePayment(request.paymentKey());
            paymentRepository.save(payment);
            order.updateStatus(OrderStatus.PAID);

        } catch (Exception e) {
            log.error("❌ 토스 결제 승인 실패: {}", e.getMessage());
            throw new IllegalStateException("결제 승인 중 에러 발생: " + e.getMessage());
        }
    }

    /**
     * 결제 상세 조회
     */
    public PaymentResponse getPaymentDetail(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제 정보입니다: " + paymentId));
        return PaymentResponse.from(payment);
    }

    /**
     * 주문별 결제 목록 조회
     */
    public List<PaymentResponse> getPaymentsByOrderNumber(String orderNumber) {
        // 주문 존재 여부 검증
        if (!orderRepository.existsByOrderNumber(orderNumber)) {
            throw new IllegalArgumentException("존재하지 않는 주문 번호입니다: " + orderNumber);
        }

        return paymentRepository.findAllByOrderOrderNumber(orderNumber)
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }
}