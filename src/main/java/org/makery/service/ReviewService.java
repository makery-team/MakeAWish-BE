package org.makery.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.makery.domain.Order;
import org.makery.domain.Review;
import org.makery.domain.Store;
import org.makery.domain.User;
import org.makery.dto.ReviewAiSummaryResponse;
import org.makery.dto.ReviewReplyRequest;
import org.makery.dto.ReviewRequest;
import org.makery.dto.ReviewResponse;
import org.makery.repository.OrderRepository;
import org.makery.repository.ReviewRepository;
import org.makery.repository.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final AiClient aiClient;

    /**
     * 1. 기존 매장별 리뷰 목록 조회 (보존)
     */
    public Page<ReviewResponse> getReviewsByStore(Long storeId, Pageable pageable) {
        return reviewRepository.findByStoreId(storeId, pageable)
                .map(ReviewResponse::from);
    }

    /**
     * 2. [신규] 특정 주문/포트폴리오 기반 리뷰(댓글) 작성
     */
    @Transactional
    public ReviewResponse createReview(Long orderId, User user, ReviewRequest request) {
        // 해당 주문이 존재하는지 및 로그인한 유저가 주문자가 맞는지 검증
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문 항목입니다."));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인의 주문 건에 대해서만 리뷰를 남길 수 있습니다.");
        }

        // 해당 주문으로 이미 작성된 리뷰가 있는지 1:1 무결성 체크
        if (reviewRepository.existsByOrderId(orderId)) {
            throw new IllegalStateException("이미 이 주문에 대한 리뷰가 존재합니다.");
        }

        Review review = Review.builder()
                .content(request.content())
                .rating(request.rating())
                .imageUrl(request.imageUrl())
                .order(order)
                .store(order.getStore()) // 주문이 들어간 매장 자동 바인딩
                .build();

        Review savedReview = reviewRepository.save(review);
        return ReviewResponse.from(savedReview);
    }

    /**
     * 3. [신규] 리뷰 수정 (소유권 검증 포함)
     */
    @Transactional
    public ReviewResponse updateReview(Long reviewId, User user, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다. ID: " + reviewId));

        // 작성자 본인 대조 검증 (Review -> Order -> User 순으로 낚아챔)
        if (!review.getOrder().getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        // 엔티티 비즈니스 메서드 호출 (더티 체킹 자동 반영)
        review.updateReview(request.content(), request.rating(), request.imageUrl());
        return ReviewResponse.from(review);
    }

    /**
     * 4. [신규] 리뷰 삭제 (소유권 검증 포함)
     */
    @Transactional
    public void deleteReview(Long reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다. ID: " + reviewId));

        if (!review.getOrder().getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        reviewRepository.delete(review);
    }

    /**
     * 5. [신규] 마이페이지: 내가 쓴 리뷰/댓글 목록 무한 스크롤(Slice) 조회
     */
    public Slice<ReviewResponse> getMyReviews(User user, Pageable pageable) {
        // reviewRepository에 findByOrderUserIdOrderByCreatedAtDesc 형태의 쿼리 메서드가 필요합니다.
        return reviewRepository.findByOrderUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(ReviewResponse::from);
    }

    // ==========================================
    // [사장님(Partner) 기능]
    // ==========================================

    @Transactional
    public ReviewResponse createOrUpdateReply(Long reviewId, User seller, ReviewReplyRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 리뷰입니다. ID: " + reviewId));

        // 해당 사장님의 매장에 달린 리뷰가 맞는지 권한 검증
        Store sellerStore = storeRepository.findByUserId(seller.getId())
                .orElseThrow(() -> new IllegalStateException("등록된 매장 정보가 없는 사장님 계정입니다. User ID: " + seller.getId()));

        if (!review.getStore().getId().equals(sellerStore.getId())) {
            throw new SecurityException("본인 매장의 리뷰에 대해서만 답글을 작성할 수 있습니다.");
        }

        // 엔티티 업데이트 (Dirty Checking)
        review.updateReply(request.getReplyContent());
        return ReviewResponse.from(review);
    }

    @Transactional
    public void deleteReply(Long reviewId, User seller) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 리뷰입니다. ID: " + reviewId));

        Store sellerStore = storeRepository.findByUserId(seller.getId())
                .orElseThrow(() -> new IllegalStateException("등록된 매장 정보가 없는 사장님 계정입니다. User ID: " + seller.getId()));

        if (!review.getStore().getId().equals(sellerStore.getId())) {
            throw new SecurityException("본인 매장의 리뷰 답글만 삭제할 수 있습니다.");
        }

        review.deleteReply();
    }

    /**
     * 특정 매장의 전체 리뷰를 조회하여 AI 요약 반환
     */
    public ReviewAiSummaryResponse getStoreReviewSummary(Long storeId) {
        // 1. 매장 존재 여부 검증
        if (!storeRepository.existsById(storeId)) {
            throw new EntityNotFoundException("존재하지 않는 매장입니다. ID: " + storeId);
        }

        // 2. 해당 매장의 리뷰 목록 조회
        List<Review> reviews = reviewRepository.findByStoreId(storeId);

        // 리뷰가 없는 경우 기본 응답 반환
        if (reviews.isEmpty()) {
            return ReviewAiSummaryResponse.builder()
                    .storeId(storeId)
                    .totalReviewCount(0)
                    .summary("아직 등록된 리뷰가 없습니다.")
                    .positivePoints(List.of())
                    .negativePoints(List.of())
                    .build();
        }

        // 3. 리뷰 텍스트만 추출
        List<String> reviewContents = reviews.stream()
                .map(Review::getContent)
                .toList();

        // 4. AI 서버 호출하여 요약 데이터 수신
        ReviewAiSummaryResponse aiResult = aiClient.getReviewSummary(reviewContents);

        // 5. 응답 데이터 조립 반환
        return ReviewAiSummaryResponse.builder()
                .storeId(storeId)
                .totalReviewCount(reviews.size())
                .summary(aiResult.getSummary())
                .positivePoints(aiResult.getPositivePoints())
                .negativePoints(aiResult.getNegativePoints())
                .build();
    }
}