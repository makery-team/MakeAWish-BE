package org.makery.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.makery.domain.PrincipalDetails;
import org.makery.domain.Store;
import org.makery.domain.User;
import org.makery.dto.*;
import org.makery.service.PortfolioService;
import org.makery.service.ReviewService;
import org.makery.service.StoreService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stores") // 💡 /api/stores 로 통일
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final PortfolioService portfolioService;
    private final ReviewService reviewService;

    /**
     * 내 매장 정보 조회 API
     * GET /api/stores/me
     */
    @GetMapping("/me")
    public ResponseEntity<MyStoreResponse> getMyStore(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        MyStoreResponse response = storeService.getMyStore(principalDetails.user().getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 1. 매장 목록 조회 (통합 API)
     * GET /api/stores
     */
    @GetMapping
    public ResponseEntity<List<StoreResponse>> getStores(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false, defaultValue = "3.0") Double radius
    ) {
        List<Store> results;
        if (lat != null && lng != null) {
            results = storeService.getNearbyStores(lat, lng, radius);
        } else {
            results = storeService.searchStores(query);
        }

        List<StoreResponse> response = results.stream()
                .map(StoreResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * 2. 매장 상세 조회 API
     * GET /api/stores/{storeId}
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreResponse> getStoreDetail(@PathVariable("storeId") Long storeId) {
        Store store = storeService.getStoreById(storeId);
        return ResponseEntity.ok(StoreResponse.from(store));
    }

    /**
     * 3. 매장별 포트폴리오 목록 조회 API (페이징)
     * GET /api/stores/{storeId}/portfolios
     */
    @GetMapping("/{storeId}/portfolios")
    public ResponseEntity<Page<PortfolioResponse>> getStorePortfolios(
            @PathVariable("storeId") Long storeId,
            @PageableDefault(size = 12, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PortfolioResponse> portfolios = portfolioService.getPortfoliosByStore(storeId, pageable);
        return ResponseEntity.ok(portfolios);
    }

    /**
     * 매장별 주문서 양식(스키마) 조회 API
     * GET /api/stores/{storeId}/order-schema
     */
    @GetMapping("/{storeId}/order-schema")
    public ResponseEntity<OrderSchemaResponse> getOrderSchema(@PathVariable("storeId") Long storeId) {
        OrderSchemaResponse response = storeService.getOrderSchemaByStoreId(storeId);
        return ResponseEntity.ok(response);
    }

    /**
     * 매장별 주문서 양식(스키마) 생성 API
     * POST /api/stores/{storeId}/order-schema
     */
    @PostMapping("/{storeId}/order-schema")
    public ResponseEntity<OrderSchemaResponse> createOrderSchema(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("storeId") Long storeId,
            @RequestBody OrderSchemaSaveRequest request) {
        OrderSchemaResponse response = storeService.createOrUpdateOrderSchema(storeId, request, principalDetails.user());
        return ResponseEntity.ok(response);
    }

    /**
     * 매장별 주문서 양식(템플릿) 수정 API
     * PATCH /api/stores/{storeId}/order-template
     */
    @PatchMapping("/{storeId}/order-template")
    public ResponseEntity<OrderSchemaResponse> updateOrderTemplate(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("storeId") Long storeId,
            @RequestBody OrderSchemaSaveRequest request) {
        OrderSchemaResponse response = storeService.createOrUpdateOrderSchema(storeId, request, principalDetails.user());
        return ResponseEntity.ok(response);
    }

    /**
     * 매장 프로필 정보 수정
     * PATCH /api/stores/profile
     */
    @PatchMapping("/profile")
    public ResponseEntity<Void> updateStoreProfile(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody StoreProfileUpdateRequest request) {

        User currentSeller = principalDetails.user();
        storeService.updateStoreProfile(currentSeller, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 프로필 개선 제안 요청
     * GET /api/stores/ai/profile-suggest
     */
    @GetMapping("/ai/profile-suggest")
    public ResponseEntity<StoreAiProfileSuggestResponse> suggestProfileImprovement(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return ResponseEntity.ok(storeService.suggestProfileImprovement(principalDetails.user()));
    }

    /**
     * 소개글 자동 생성
     * POST /api/stores/ai/generate-bio
     */
    @PostMapping("/ai/generate-bio")
    public ResponseEntity<StoreAiBioGenerateResponse> generateBio(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody StoreAiBioGenerateRequest request
    ) {
        return ResponseEntity.ok(storeService.generateBio(principalDetails.user(), request));
    }


    /**
     * 특정 매장 리뷰 요약 조회 API
     * GET /api/stores/{storeId}/reviews/summary
     */
    @GetMapping("/{storeId}/reviews/summary")
    public ResponseEntity<ReviewAiSummaryResponse> getStoreReviewSummary(@PathVariable Long storeId) {
        ReviewAiSummaryResponse response = reviewService.getStoreReviewSummary(storeId);
        return ResponseEntity.ok(response);
    }
}