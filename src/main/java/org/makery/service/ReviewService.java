package org.makery.service;

import lombok.RequiredArgsConstructor;
import org.makery.dto.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.makery.repository.ReviewRepository; // 💡 이 줄이 꼭 있어야 합니다!

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public Page<ReviewResponse> getReviewsByStore(Long storeId, Pageable pageable) {
        // DB에서 리뷰 페이지를 가져와서 DTO 페이지로 변환합니다.
        return reviewRepository.findByStoreId(storeId, pageable)
                .map(ReviewResponse::from);
    }
}