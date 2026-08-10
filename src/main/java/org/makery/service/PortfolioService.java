package org.makery.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.makery.client.AiClient;
import org.makery.domain.*;
import org.makery.dto.*;
import org.makery.repository.PortfolioRepository;
import org.makery.repository.ProductRepository;
import org.makery.repository.StoreRepository;
import org.makery.repository.TagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final ProductRepository productRepository;
    private final TagRepository tagRepository;
    private final StoreRepository storeRepository;
    private final AiClient aiClient;

    /**
     * 💡 추가된 메서드: 특정 매장(storeId)의 포트폴리오를 페이징 처리해서 가져오기
     */
    public Page<PortfolioResponse> getPortfoliosByStore(Long storeId, Pageable pageable) {
        // 1. Repository에서 Page<Portfolio>를 가져옵니다.
        // 2. .map()을 사용해 각 엔티티를 PortfolioResponse(DTO)로 변환합니다.
        return portfolioRepository.findByStoreId(storeId, pageable)
                .map(PortfolioResponse::from);
    }

    public List<PortfolioResponse> search(Set<Tag> tags, String sort, Pageable pageable) {
        // 1. DB에서 기본적인 데이터와 페이징 처리만 수행
        // 만약 repository의 메서드가 query를 필수로 받는다면 null을 넘겨줍니다.
        List<Portfolio> portfolios = portfolioRepository.findAll(pageable).getContent();

        // 2. 서비스 단에서 태그 매칭 필터링 수행
        return portfolios.stream()
                .filter(p -> isTagMatch(p.getTags(), tags))
                .map(PortfolioResponse::from)
                .toList();
    }

    // 첫 번째 파라미터를 Collection<Tag> 또는 구체적인 타입으로 변경
    private boolean isTagMatch(Set<Tag> portfolioTags, Set<Tag> searchTags) {
        // 1. 검색 조건으로 들어온 태그가 없으면(null 또는 빈 값) 필터링 없이 통과
        if (searchTags == null || searchTags.isEmpty()) {
            return true;
        }

        // 2. 포트폴리오의 태그 세트가 검색 조건의 태그 세트를 모두 포함하는지 확인
        // Set 타입끼리는 containsAll 메서드를 통해 한 번에 비교가 가능합니다.
        return portfolioTags != null && portfolioTags.containsAll(searchTags);
    }

    /**
     * 무한 스크롤 홈 피드 태그 필터 (정렬 옵션 추가)
     */
    public Slice<PortfolioFeedResponse> searchFeedByTags(List<String> tags, String sortType, Pageable pageable) {
        if (tags == null || tags.isEmpty()) {
            return getFeed(sortType, pageable);
        }

        Long tagCount = (long) tags.size();
        if ("latest".equalsIgnoreCase(sortType)) {
            return portfolioRepository.findByTagsOrderByCreatedAtDesc(tags, tagCount, pageable)
                    .map(PortfolioFeedResponse::from);
        } else {
            return portfolioRepository.findByTagsOrderByLikeCountDesc(tags, tagCount, pageable)
                    .map(PortfolioFeedResponse::from);
        }
    }

    /**
     * 피드 조회 (최신순 또는 인기순)
     */
    public Slice<PortfolioFeedResponse> getFeed(String sortType, Pageable pageable) {
        if ("latest".equalsIgnoreCase(sortType)) {
            return portfolioRepository.findAllByOrderByCreatedAtDesc(pageable)
                    .map(PortfolioFeedResponse::from);
        } else {
            return portfolioRepository.findAllByOrderByLikeCountDesc(pageable)
                    .map(PortfolioFeedResponse::from);
        }
    }

    /**
     * 트렌딩 태그 상위 N개 조회
     */
    public List<String> getTrendingTags(int limit) {
        return portfolioRepository.findTrendingTagNames(PageRequest.of(0, limit));
    }

    // ==========================================
    // [사장님(Partner) 기능]
    // ==========================================

    /**
     * 등록 전 AI 이미지 태그 추천 요청
     */
    public List<String> recommendTagsWithAi(PortfolioTagRecommendRequest request) {
        AiTagRequest aiRequest = new AiTagRequest(request.getImageUrl());
        AiTagResponse aiResponse = aiClient.generateTags(aiRequest);

        if (aiResponse == null || aiResponse.tags() == null) {
            return List.of();
        }

        return aiResponse.tags();
    }

    /**
     * 포트폴리오 신규 등록
     */
    @Transactional
    public PortfolioResponse registerPortfolio(User seller, PortfolioRegisterRequest request) {
        Store store = storeRepository.findByUserId(seller.getId())
                .orElseThrow(() -> new IllegalStateException("등록된 매장 정보가 없는 사장님 계정입니다. User ID: " + seller.getId()));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상품 카테고리 ID입니다. ID: " + request.getProductId()));

        Set<Tag> tags = new HashSet<>();
        if (request.getTags() != null) {
            for (String tagName : request.getTags()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
                tags.add(tag);
            }
        }

        Portfolio portfolio = Portfolio.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .product(product)
                .store(store)
                .tags(tags)
                .isInpaintingAllowed(request.getIsInpaintingAllowed() != null ? request.getIsInpaintingAllowed() : true)
                .build();

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        return PortfolioResponse.from(savedPortfolio);
    }

    /**
     * 3. 포트폴리오 정보 수정
     */
    @Transactional
    public void updatePortfolio(Long portfolioId, PortfolioUpdateRequest request) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 포트폴리오입니다. ID: " + portfolioId));

        if (request.getTitle() != null) portfolio.setTitle(request.getTitle());
        if (request.getDescription() != null) portfolio.setDescription(request.getDescription());
        if (request.getImageUrl() != null) portfolio.setImageUrl(request.getImageUrl());
        if (request.getIsInpaintingAllowed() != null) portfolio.setInpaintingAllowed(request.getIsInpaintingAllowed());

        if (request.getTags() != null) {
            Set<Tag> updatedTags = request.getTags().stream()
                    .map(tagName -> tagRepository.findByName(tagName)
                            .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build())))
                    .collect(Collectors.toSet());
            portfolio.setTags(updatedTags);
        }
    }
}
