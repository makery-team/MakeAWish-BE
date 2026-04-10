package org.makery.controller;

import lombok.RequiredArgsConstructor;
import org.makery.domain.OrderStatus;
import org.makery.domain.PrincipalDetails;
import org.makery.dto.OrderDetailResponse;
import org.makery.dto.OrderRequest;
import org.makery.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
                                           @RequestBody OrderRequest req) {

        Long orderId = orderService.createOrder(principalDetails.getUser().getId(), req);
        return ResponseEntity.ok(orderId);
    }

    /**
     * 본인의 주문 상세 조회 API
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
}
