package org.makery.controller;

import lombok.RequiredArgsConstructor;
import org.makery.domain.Store;
import org.makery.dto.OrderSchemaResponse; // 💡 추가됨!
import org.makery.dto.PortfolioResponse;
import org.makery.dto.StoreResponse;
import org.makery.service.PortfolioService;
import org.makery.service.StoreService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final PortfolioService portfolioService;

    /**
     * 1. 매장 목록 조회 (통합 API)
     * 일반 검색 또는 지도 기반 반경 조회
     */
    @GetMapping("/stores")
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
     */
    @GetMapping("/stores/{storeId}")
    public ResponseEntity<StoreResponse> getStoreDetail(@PathVariable Long storeId) {
        Store store = storeService.getStoreById(storeId);
        return ResponseEntity.ok(StoreResponse.from(store));
    }

    /**
     * 3. 매장별 포트폴리오 목록 조회 API (페이징)
     */
    @GetMapping("/stores/{storeId}/portfolios")
    public ResponseEntity<Page<PortfolioResponse>> getStorePortfolios(
            @PathVariable Long storeId,
            @PageableDefault(size = 12, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PortfolioResponse> portfolios = portfolioService.getPortfoliosByStore(storeId, pageable);
        return ResponseEntity.ok(portfolios);
    }

    /**
     * 4. 매장별 주문서 양식(스키마) 조회 API
     * GET /api/stores/{storeId}/order-schema
     * 💡 새로 추가된 메서드입니다!
     */
    @GetMapping("/stores/{storeId}/order-schema")
    public ResponseEntity<OrderSchemaResponse> getOrderSchema(@PathVariable Long storeId) {
        OrderSchemaResponse response = storeService.getOrderSchema(storeId);
        return ResponseEntity.ok(response);
    }
}