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

    @Query("select p from Portfolio p join fetch p.store join fetch p.tags")
    Slice<Portfolio> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("select p from Portfolio p " +
            "join p.tags t " +
            "where t.name in :tagNames " +
            "group by p.id " +
            "having count(t.id) = :tagCount")
    Slice<Portfolio> findByTags(@Param("tagNames") List<String> tagNames,
                                @Param("tagCount") Long tagCount,
                                Pageable pageable);
}
