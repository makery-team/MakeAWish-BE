package org.makery.repository;

import org.makery.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 💡 특정 매장(storeId)의 리뷰를 페이징 처리해서 가져오기
    Page<Review> findByStoreId(Long storeId, Pageable pageable);
}