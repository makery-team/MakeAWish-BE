package org.makery.controller;

import lombok.RequiredArgsConstructor;
import org.makery.domain.OrderStatus;
import org.makery.domain.PrincipalDetails;
import org.makery.dto.MessageDraftResponse;
import org.makery.dto.OrderCreateRequest;
import org.makery.dto.OrderDetailResponse;
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

        Long orderId = orderService.createOrder(principalDetails.user().getId(), orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
    }

    /**
     * 주문 상세 정보 조회 API
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                              @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderDetail(orderId,
                principalDetails.user().getId(),
                principalDetails.user().getUserRole()));
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
                principalDetails.user().getId(),
                status
        );

        return ResponseEntity.ok().build();
    }

    /**
     * 내 주문 목록 조회 API (date=today 지원)
     */
    @GetMapping
    public ResponseEntity<List<OrderSummaryResponse>> getMyOrders(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam(required = false) String date) {

        List<OrderSummaryResponse> responses = orderService.getMyOrders(
                principalDetails.user().getId(),
                principalDetails.user().getUserRole(),
                date
        );

        return ResponseEntity.ok(responses);
    }

    /**
     * 주문 상태 변경 API (JSON 바디 수락/거절 등 수신)
     * PATCH /api/orders/{orderId}
     */
    @PatchMapping("/{orderId}")
    public ResponseEntity<Void> updateStatusByBody(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long orderId,
            @RequestBody org.makery.dto.OrderStatusUpdateRequest request) {

        orderService.updateOrderStatusByBody(
                orderId,
                principalDetails.user().getId(),
                request.status()
        );

        return ResponseEntity.ok().build();
    }

    /**
     * (AI) 메시지 초안 생성 API
     */
    @PostMapping("/{orderId}/message-drafts")
    public ResponseEntity<MessageDraftResponse> generateMessageDraft(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long orderId) {

        MessageDraftResponse response = orderService.generateMessageDraft(orderId, principalDetails.user().getId());
        return ResponseEntity.ok(response);
    }
}
