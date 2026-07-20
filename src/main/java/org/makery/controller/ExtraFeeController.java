package org.makery.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.makery.domain.PrincipalDetails;
import org.makery.domain.User;
import org.makery.dto.ExtraFeeCreateRequest;
import org.makery.dto.ExtraFeeResponse;
import org.makery.service.ExtraFeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class ExtraFeeController {

    private final ExtraFeeService extraFeeService;

    /**
     * 1. 사장님: 주문 추가금 책정 및 등록 API
     * POST /api/orders/{orderId}/extra-fee
     */
    @PostMapping("/{orderId}/extra-fee")
    public ResponseEntity<ExtraFeeResponse> createExtraFee(
            @PathVariable("orderId") Long orderId,
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody ExtraFeeCreateRequest request) {

        User currentSeller = principalDetails.user();
        ExtraFeeResponse response = extraFeeService.updateExtraFee(orderId, currentSeller, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 2. 손님 / 사장님: 추가금 상세 및 최종 가격 조회 API
     * GET /api/orders/{orderId}/extra-fee
     */
    @GetMapping("/{orderId}/extra-fee")
    public ResponseEntity<ExtraFeeResponse> getExtraFee(
            @PathVariable("orderId") Long orderId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        User currentUser = principalDetails.user();
        ExtraFeeResponse response = extraFeeService.getExtraFee(orderId, currentUser);
        return ResponseEntity.ok(response);
    }
}
