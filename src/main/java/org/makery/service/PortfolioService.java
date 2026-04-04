package org.makery.service;

import lombok.RequiredArgsConstructor;
import org.makery.dto.PortfolioResponse;
import org.makery.repository.PortfolioRepository;
import org.springframework.data.domain.Page; // 💡 추가됨
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    /**
     * 💡 추가된 메서드: 특정 매장(storeId)의 포트폴리오를 페이징 처리해서 가져오기
     */
    public Page<PortfolioResponse> getPortfoliosByStore(Long storeId, Pageable pageable) {
        // 1. Repository에서 Page<Portfolio>를 가져옵니다.
        // 2. .map()을 사용해 각 엔티티를 PortfolioResponse(DTO)로 변환합니다.
        return portfolioRepository.findByStoreId(storeId, pageable)
                .map(PortfolioResponse::from);
    }

    /**
     * 기존 검색 로직 (유지)
     */
    public List<PortfolioResponse> search(String query, String tags, String sort, Pageable pageable) {
        return portfolioRepository.findAll().stream()
                .filter(p -> p.getTags() != null && (tags == null || p.getTags().contains(tags)))
                .map(PortfolioResponse::from)
                .toList();
    }

    // ... 기존 isTagMatch 메서드 (유지)
}