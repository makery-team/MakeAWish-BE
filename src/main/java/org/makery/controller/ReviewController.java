package org.makery.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.makery.domain.PrincipalDetails;
import org.makery.dto.ReviewReplyRequest;
import org.makery.dto.ReviewRequest;
import org.makery.dto.ReviewResponse;
import org.makery.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 1. 기존 매장별 리뷰 목록 조회 API (보존)
     * GET /api/stores/{storeId}/reviews
     */
    @GetMapping("/stores/{storeId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getStoreReviews(
            @PathVariable Long storeId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ReviewResponse> reviews = reviewService.getReviewsByStore(storeId, pageable);
        return ResponseEntity.ok(reviews);
    }

    /**
     * 2. [신규] 리뷰(댓글) 작성 API
     * POST /api/orders/{orderId}/reviews
     * 설명: 유저가 커스텀 제작 주문을 성공적으로 마친 뒤, 해당 내역 정보(orderId)를 바탕으로 리뷰를 등록합니다.
     */
    @PostMapping("/orders/{orderId}/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long orderId,
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody ReviewRequest request
    ) {
        ReviewResponse response = reviewService.createReview(orderId, principalDetails.user(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 3. [신규] 리뷰(댓글) 수정 API
     * PUT /api/reviews/{reviewId}
     */
    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody ReviewRequest request
    ) {
        ReviewResponse response = reviewService.updateReview(reviewId, principalDetails.user(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * 4. [신규] 리뷰(댓글) 삭제 API
     * DELETE /api/reviews/{reviewId}
     */
    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        reviewService.deleteReview(reviewId, principalDetails.user());
        return ResponseEntity.noContent().build(); // 204 No Content 반환
    }

    /**
     * 5. [신규] 마이페이지: 내가 작성한 모든 리뷰 목록 조회 (무한 스크롤)
     * GET /api/reviews/me?page=0&size=5
     */
    @GetMapping("/reviews/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Slice<ReviewResponse>> getMyReviews(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Slice<ReviewResponse> responses = reviewService.getMyReviews(principalDetails.user(), pageable);
        return ResponseEntity.ok(responses);
    }

    // ==========================================
    // [사장님(Partner) 기능]
    // ==========================================

    /**
     * 리뷰 답글 작성 및 수정 API
     * POST /api/reviews/{reviewId}/reply
     */
    @PostMapping("/reviews/{reviewId}/reply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponse> createOrUpdateReply(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody ReviewReplyRequest request
    ) {
        ReviewResponse response = reviewService.createOrUpdateReply(reviewId, principalDetails.user(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * 리뷰 답글 삭제 API
     * DELETE /api/reviews/{reviewId}/reply
     */
    @DeleteMapping("/reviews/{reviewId}/reply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteReply(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        reviewService.deleteReply(reviewId, principalDetails.user());
        return ResponseEntity.noContent().build();
    }
}