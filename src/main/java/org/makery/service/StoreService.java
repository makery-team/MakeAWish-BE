package org.makery.service;

import lombok.RequiredArgsConstructor;
import org.makery.domain.Store;
import org.makery.dto.OrderSchemaResponse; // 💡 추가됨!
import org.makery.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;

    /**
     * 1. 매장 검색 및 전체 조회 (텍스트 기반)
     */
    public List<Store> searchStores(String query) {
        if (query == null || query.isBlank()) {
            return storeRepository.findAll();
        }
        return storeRepository.findByNameContainingOrDescriptionContaining(query, query);
    }

    /**
     * 2. 지도 기반 반경 내 매장 조회 (위치 기반)
     */
    public List<Store> getNearbyStores(Double lat, Double lng, Double radius) {
        if (radius == null) radius = 3.0;
        return storeRepository.findNearbyStores(lat, lng, radius);
    }

    /**
     * 3. 매장 상세 조회 (ID로 찾기)
     */
    public Store getStoreById(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 매장을 찾을 수 없습니다. ID: " + storeId));
    }

    /**
     * 4. 매장별 주문 양식(스키마) 조회
     * 💡 새로 추가된 메서드입니다.
     */
    public OrderSchemaResponse getOrderSchema(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 매장을 찾을 수 없습니다. ID: " + storeId));

        // DTO 내부의 static factory 메서드(from)를 활용해 변환합니다.
        return OrderSchemaResponse.from(store);
    }
}