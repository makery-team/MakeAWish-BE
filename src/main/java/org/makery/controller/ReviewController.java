package org.makery.controller;

import lombok.RequiredArgsConstructor;
import org.makery.dto.ReviewResponse;
import org.makery.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api") // 기본 경로를 /api로 설정
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 매장별 리뷰 목록 조회 API
     * GET /api/stores/{storeId}/reviews?page=0&size=10
     * * 💡 설명: 특정 매장의 ID를 받아 그 매장에 달린 리뷰들을 페이징해서 보여줍니다.
     */
    @GetMapping("/stores/{storeId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getStoreReviews(
            @PathVariable Long storeId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // 1. 리뷰 서비스에서 데이터를 가져옵니다.
        Page<ReviewResponse> reviews = reviewService.getReviewsByStore(storeId, pageable);

        // 2. 200 OK 상태코드와 함께 결과를 반환합니다.
        return ResponseEntity.ok(reviews);
    }
}