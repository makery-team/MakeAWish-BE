package org.makery.repository;

import org.makery.domain.Portfolio;
import org.springframework.data.domain.Page; // 💡 Page 임포트 추가
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    /**
     * 💡 추가된 메서드: 특정 매장(storeId)의 포트폴리오만 페이징해서 가져오기
     * JpaRepository가 'findByStoreId'라는 이름을 보고 자동으로 쿼리를 생성합니다.
     */
    Page<Portfolio> findByStoreId(Long storeId, Pageable pageable);

    /**
     * 기존 검색 로직 (유지)
     * JSON 필드(tags)는 DB 엔진마다 문법이 다르므로 JPQL 조인 대신
     * 기본 검색만 DB에서 수행하고 태그는 서비스에서 필터링합니다.
     */
    @Query("SELECT DISTINCT p FROM Portfolio p " +
            "JOIN FETCH p.store s " +
            "WHERE (:query IS NULL OR s.name LIKE %:query% OR s.description LIKE %:query%)")
    List<Portfolio> searchByFilters(@Param("query") String query, Pageable pageable);
}