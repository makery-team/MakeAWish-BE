package org.makery.controller;

import lombok.RequiredArgsConstructor;
import org.makery.domain.OrderStatus;
import org.makery.domain.PrincipalDetails;
import org.makery.dto.OrderDetailResponse;
import org.makery.dto.OrderCreateRequest;
import org.makery.dto.OrderSummaryResponse;
import org.makery.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 생성 API
     */
    @PostMapping
    public ResponseEntity<Long> placeOrder(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                           @RequestBody OrderCreateRequest orderRequest) {

        Long orderId = orderService.createOrder(principalDetails.getUser().getId(), orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
    }

    /**
     * 주문 상세 정보 조회 API
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                              @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderDetail(orderId,
                principalDetails.getUser().getId(),
                principalDetails.getUser().getUserRole()));
    }

    /**
     * 주문 상태 변경 API (접수, 완료 등)
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Void> updateStatus(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                             @PathVariable Long orderId,
                                             @RequestParam OrderStatus status) {

        orderService.updateOrderStatus(
                orderId,
                principalDetails.getUser().getId(),
                status
        );

        return ResponseEntity.ok().build();
    }

    /**
     * 내 주문 목록 조회 API
     */
    @GetMapping
    public ResponseEntity<List<OrderSummaryResponse>> getMyOrders(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        List<OrderSummaryResponse> responses = orderService.getMyOrders(
                principalDetails.getUser().getId(),
                principalDetails.getUser().getUserRole()
        );

        return ResponseEntity.ok(responses);
    }
}
