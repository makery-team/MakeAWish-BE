package org.makery.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.makery.domain.Product; // 💡 Product 임포트 추가
import org.makery.domain.SellerProfile;
import org.makery.domain.Store;
import org.makery.domain.User;
import org.makery.dto.*;
import org.makery.repository.ProductRepository; // 💡 ProductRepository 주입 필요
import org.makery.repository.StoreRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final AiClient aiClient;

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
     * 주문서 양식(스키마) 생성 및 수정 (사장님 권한)
     */
    @Transactional
    public OrderSchemaResponse createOrUpdateOrderSchema(Long storeId, OrderSchemaSaveRequest request, User currentSeller) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 매장을 찾을 수 없습니다. ID: " + storeId));

        if (store.getSellerProfile() == null || !store.getSellerProfile().getUser().getId().equals(currentSeller.getId())) {
            throw new AccessDeniedException("본인 매장의 주문서 양식만 수정할 수 있습니다.");
        }

        Product product;
        if (request.productId() != null) {
            product = productRepository.findById(request.productId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 상품을 찾을 수 없습니다. ID: " + request.productId()));
            if (!product.getStore().getId().equals(storeId)) {
                throw new IllegalArgumentException("해당 매장의 상품이 아닙니다.");
            }
        } else {
            // 매장의 첫 번째 상품 또는 대표 상품의 스키마 업데이트
            if (store.getProducts().isEmpty()) {
                throw new IllegalStateException("매장에 등록된 상품이 없어 양식을 생성할 수 없습니다.");
            }
            product = store.getProducts().get(0);
        }

        product.setOrderSchema(request.orderSchema());
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
        store.setAddress(request.getAddress());
        store.setPhone(request.getPhone());
        store.setHours(request.getHours());
        store.setNotice(request.getNotice());
        store.setCautionNotice(request.getCautionNotice());
    }

    /**
     * 프로필 개선 제안 요청
     */
    public StoreAiProfileSuggestResponse suggestProfileImprovement(User seller) {
        Store store = getSellerStore(seller);

        Map<String, Object> storeData = Map.of(
                "storeName", store.getName(),
                "description", store.getDescription() != null ? store.getDescription() : "",
                "notice", store.getNotice() != null ? store.getNotice() : "",
                "cautionNotice", store.getCautionNotice() != null ? store.getCautionNotice() : ""
        );

        return aiClient.suggestProfileImprovement(storeData);
    }

    /**
     * 소개글 자동 생성
     */
    public StoreAiBioGenerateResponse generateBio(User seller, StoreAiBioGenerateRequest request) {
        Store store = getSellerStore(seller);

        Map<String, String> requestData = Map.of(
                "storeName", store.getName(),
                "keywords", request.keywords() != null ? request.keywords() : "",
                "concept", request.concept() != null ? request.concept() : ""
        );

        return aiClient.generateBio(requestData);
    }

    private Store getSellerStore(User seller) {
        return storeRepository.findByUserId(seller.getId())
                .orElseThrow(() -> new EntityNotFoundException("등록된 매장 정보가 없는 사장님 계정입니다. User ID: " + seller.getId()));
    }

    /**
     * 내 매장 정보 조회
     */
    @Transactional(readOnly = true)
    public MyStoreResponse getMyStore(Long userId) {
        Store store = storeRepository.findBySellerProfileUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("등록된 매장 정보를 찾을 수 없습니다. userId: " + userId));

        return MyStoreResponse.from(store);
    }
}