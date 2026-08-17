package org.makery.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.makery.dto.PaymentResponse;
import org.makery.dto.TossPaymentConfirmRequest;
import org.makery.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 토스 결제 승인 요청
     */
    @PostMapping("/toss/confirm")
    public ResponseEntity<Void> confirmTossPayment(@Valid @RequestBody TossPaymentConfirmRequest request) {
        paymentService.confirmTossPayment(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 결제 상세 조회
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentDetail(@PathVariable("paymentId") Long paymentId) {
        PaymentResponse response = paymentService.getPaymentDetail(paymentId);
        return ResponseEntity.ok(response);
    }

    /**
     * 주문 결제 목록 조회 (주문 번호 기준)
     */
    @GetMapping("/orders/{orderNumber}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrderNumber(@PathVariable("orderNumber") String orderNumber) {
        List<PaymentResponse> responses = paymentService.getPaymentsByOrderNumber(orderNumber);
        return ResponseEntity.ok(responses);
    }
}