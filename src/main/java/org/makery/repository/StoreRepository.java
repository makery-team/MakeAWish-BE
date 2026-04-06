package org.makery.repository;

import org.makery.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    /**
     * 1. 기존 기능: 매장명 또는 매장 설명에 검색어가 포함된 매장 리스트 조회
     * (Spring Data JPA가 메서드 이름을 분석해서 쿼리를 자동 생성합니다.)
     */
    List<Store> findByNameContainingOrDescriptionContaining(String name, String description);

    /**
     * 2. 신규 기능: 하버사인(Haversine) 공식을 이용한 반경 내 매장 조회
     * 6371은 지구의 반지름(km)입니다.
     * Native Query를 사용하여 DB의 수학 함수(acos, cos, radians 등)를 직접 호출합니다.
     */
    @Query(value = "SELECT *, (6371 * acos(cos(radians(:lat)) * cos(radians(latitude)) " +
            "* cos(radians(longitude) - radians(:lng)) + sin(radians(:lat)) " +
            "* sin(radians(latitude)))) AS distance " +
            "FROM stores " +
            "HAVING distance <= :radius " +
            "ORDER BY distance", nativeQuery = true)
    List<Store> findNearbyStores(@Param("lat") Double lat,
                                 @Param("lng") Double lng,
                                 @Param("radius") Double radius);
}