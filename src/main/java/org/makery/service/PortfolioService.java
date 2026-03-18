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

        // 1. DB에서는 텍스트 기반 검색 수행
        List<Portfolio> portfolios = portfolioRepository.searchByFilters(query, pageable);

        // 2. 서비스 단에서 태그 필터링 수행
        return portfolios.stream()
                .filter(p -> isTagMatch(p.getTags(), tags)) // p.getTags()는 Set<Tag>를 반환한다고 가정
                .map(PortfolioResponse::from)
                .toList();
    }

    // 첫 번째 파라미터를 Collection<Tag> 또는 구체적인 타입으로 변경
    private boolean isTagMatch(java.util.Set<org.makery.domain.Tag> portfolioTags, List<String> searchTags) {
        // 검색 태그가 없으면 필터링 통과
        if (searchTags == null || searchTags.isEmpty()) {
            return true;
        }

        if (portfolioTags == null || portfolioTags.isEmpty()) {
            return false;
        }

        // Tag 객체에서 name을 추출하여 검색 태그 리스트와 비교
        return portfolioTags.stream()
                .map(org.makery.domain.Tag::getName) // Tag 엔티티의 getName() 메서드 사용
                .anyMatch(searchTags::contains);
    }
}
