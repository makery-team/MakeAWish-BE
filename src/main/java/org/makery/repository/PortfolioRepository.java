package org.makery.repository;

import org.makery.domain.Portfolio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Page<Portfolio> findByStoreId(Long storeId, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Portfolio p " +
            "JOIN FETCH p.store s " +
            "WHERE (:query IS NULL OR s.name LIKE %:query% OR s.description LIKE %:query%)")
    List<Portfolio> searchByFilters(@Param("query") String query, Pageable pageable);

    @Query("select p from Portfolio p join fetch p.store join fetch p.tags order by p.createdAt desc")
    Slice<Portfolio> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("select p from Portfolio p join fetch p.store join fetch p.tags order by p.likeCount desc")
    Slice<Portfolio> findAllByOrderByLikeCountDesc(Pageable pageable);

    @Query("select p from Portfolio p " +
            "join p.tags t " +
            "where t.name in :tagNames " +
            "group by p.id " +
            "having count(t.id) = :tagCount " +
            "order by p.createdAt desc")
    Slice<Portfolio> findByTagsOrderByCreatedAtDesc(@Param("tagNames") List<String> tagNames,
                                                    @Param("tagCount") Long tagCount,
                                                    Pageable pageable);

    @Query("select p from Portfolio p " +
            "join p.tags t " +
            "where t.name in :tagNames " +
            "group by p.id " +
            "having count(t.id) = :tagCount " +
            "order by p.likeCount desc")
    Slice<Portfolio> findByTagsOrderByLikeCountDesc(@Param("tagNames") List<String> tagNames,
                                                    @Param("tagCount") Long tagCount,
                                                    Pageable pageable);

    @Query("SELECT t.name FROM Portfolio p " +
            "JOIN p.tags t " +
            "GROUP BY t.name " +
            "ORDER BY COUNT(p.id) DESC")
    List<String> findTrendingTagNames(Pageable pageable);

    /**
     * AI 에이전트 추천용: 태그 리스트에 포함된 포트폴리오를 중복 없이 조회
     */
    @Query("SELECT DISTINCT p FROM Portfolio p " +
            "JOIN FETCH p.store s " +
            "JOIN p.tags t " +
            "WHERE t.name IN :tagNames")
    List<Portfolio> findByTagNames(@Param("tagNames") List<String> tagNames);
}
