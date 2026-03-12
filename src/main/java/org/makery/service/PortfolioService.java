package org.makery.service;

import lombok.RequiredArgsConstructor;
import org.makery.domain.Portfolio;
import org.makery.dto.PortfolioResponse;
import org.makery.repository.PortfolioRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    public List<PortfolioResponse> search(String query, List<String> tags, String sort, Pageable pageable) {

        // 1. DB에서는 텍스트 기반 검색만 수행
        List<Portfolio> portfolios = portfolioRepository.searchByFilters(query, pageable);

        // 2. 서비스 단에서 태그 필터링 수행 (JSON 타입 호환성 문제 해결)
        return portfolios.stream()
                .filter(p -> isTagMatch(p.getTags(), tags))
                .map(PortfolioResponse::from)
                .toList();
    }

    private boolean isTagMatch(List<String> portfolioTags, List<String> searchTags) {
        // 검색 태그가 없으면 필터링 통과
        if (searchTags == null || searchTags.isEmpty()) {
            return true;
        }
        // 포트폴리오 태그 중 검색 태그가 하나라도 포함되어 있는지 확인
        return portfolioTags.stream().anyMatch(searchTags::contains);
    }
}