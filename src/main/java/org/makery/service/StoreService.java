package org.makery.service;

import lombok.RequiredArgsConstructor;
import org.makery.domain.Product; // 💡 Product 임포트 추가
import org.makery.domain.Store;
import org.makery.dto.OrderSchemaResponse;
import org.makery.repository.ProductRepository; // 💡 ProductRepository 주입 필요
import org.makery.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final ProductRepository productRepository; // 💡 상품 조회를 위해 추가

    /**
     * 1. 매장 검색 및 전체 조회 (기존 유지)
     */
    public List<Store> searchStores(String query) {
        if (query == null || query.isBlank()) {
            return storeRepository.findAll();
        }
        return storeRepository.findByNameContainingOrDescriptionContaining(query, query);
    }

    /**
     * 2. 지도 기반 반경 내 매장 조회 (기존 유지)
     */
    public List<Store> getNearbyStores(Double lat, Double lng, Double radius) {
        if (radius == null) radius = 3.0;
        return storeRepository.findNearbyStores(lat, lng, radius);
    }

    /**
     * 3. 매장 상세 조회 (기존 유지)
     */
    public Store getStoreById(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 매장을 찾을 수 없습니다. ID: " + storeId));
    }

    /**
     * 💡 핵심 수정: 매장 ID가 아닌 '상품(카테고리) ID'로 양식을 조회합니다.
     * 메서드명도 이해하기 쉽게 getProductSchema로 변경하는 것을 추천합니다.
     */
    public OrderSchemaResponse getOrderSchema(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품(카테고리)을 찾을 수 없습니다. ID: " + productId));

        // 💡 이제 OrderSchemaResponse.from()은 Product를 인자로 받습니다.
        return OrderSchemaResponse.from(product);
    }
}