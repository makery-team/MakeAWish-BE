package org.makery.service;

import lombok.RequiredArgsConstructor;
import org.makery.domain.Product; // 💡 Product 임포트 추가
import org.makery.domain.SellerProfile;
import org.makery.domain.Store;
import org.makery.domain.User;
import org.makery.dto.OrderSchemaRequest;
import org.makery.dto.OrderSchemaResponse;
import org.makery.dto.StoreProfileUpdateRequest;
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
    private final ProductRepository productRepository;

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

    /**
     * 사장님의 매장 프로필 및 안내사항 수정
     */
    @Transactional
    public void updateStoreProfile(User seller, StoreProfileUpdateRequest request) {
        Store store = storeRepository.findByUserId(seller.getId())
                .orElseThrow(() -> new IllegalStateException("등록된 매장 정보가 없는 사장님 계정입니다. User ID: " + seller.getId()));

        store.setName(request.getName());
        store.setDescription(request.getDescription());
        store.setHours(request.getHours());
        store.setNotice(request.getNotice());
        store.setCautionNotice(request.getCautionNotice());
    }

    /**
     * 매장 주문서 양식(스키마) 저장/수정
     */
    @Transactional
    public void createOrderSchema(Long sellerId, Long storeId, OrderSchemaRequest request) {
        Store store = storeRepository.findByUserId(sellerId)
                .orElseThrow(() -> new IllegalStateException("등록된 매장 정보가 없는 사장님 계정입니다."));

        if (!store.getId().equals(storeId)) {
            throw new org.springframework.security.access.AccessDeniedException("본인 매장의 주문서 양식만 수정할 수 있습니다.");
        }

        Long productId = request.getProductId();
        Product product;
        if (productId != null) {
            product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 상품을 찾을 수 없습니다. ID: " + productId));
            if (!product.getStore().getId().equals(storeId)) {
                throw new IllegalArgumentException("해당 상품은 이 매장의 상품이 아닙니다.");
            }
        } else {
            // fallback: 매장의 첫 번째 상품의 양식을 업데이트
            if (store.getProducts().isEmpty()) {
                throw new IllegalArgumentException("매장에 등록된 상품이 없습니다. 상품을 먼저 생성해주세요.");
            }
            product = store.getProducts().get(0);
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, Object> schemaMap = objectMapper.readValue(request.getSchemaData(), java.util.Map.class);
            product.setOrderSchema(schemaMap);
            productRepository.save(product);
        } catch (Exception e) {
            throw new IllegalArgumentException("올바르지 않은 JSON 스키마 형식입니다.", e);
        }
    }
}