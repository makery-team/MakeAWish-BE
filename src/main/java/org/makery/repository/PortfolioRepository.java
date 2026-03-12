package org.makery.repository;

import org.makery.domain.Portfolio;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    /**
     * JSON 필드(tags)는 DB 엔진마다 문법이 다르므로 JPQL 조인 대신
     * 기본 검색만 DB에서 수행하고 태그는 서비스에서 필터링합니다.
     */
    @Query("SELECT DISTINCT p FROM Portfolio p " +
            "JOIN FETCH p.store s " +
            "WHERE (:query IS NULL OR s.name LIKE %:query% OR s.description LIKE %:query%)")
    List<Portfolio> searchByFilters(@Param("query") String query, Pageable pageable);
}