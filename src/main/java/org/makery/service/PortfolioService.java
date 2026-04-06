package org.makery.service;

import lombok.RequiredArgsConstructor;
import org.makery.domain.Portfolio;
import org.makery.domain.Tag;
import org.makery.dto.PortfolioResponse;
import org.makery.repository.PortfolioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

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
}
