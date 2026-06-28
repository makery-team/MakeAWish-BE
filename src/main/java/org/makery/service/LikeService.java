package org.makery.service;

import lombok.RequiredArgsConstructor;
import org.makery.domain.Like;
import org.makery.domain.Portfolio;
import org.makery.domain.User;
import org.makery.dto.PortfolioResponse;
import org.makery.repository.LikeRepository;
import org.makery.repository.PortfolioRepository;
import org.makery.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;

    // [기능 1] 좋아요 추가
    @Transactional
    public void addLike(Long userId, Long portfolioId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + userId));
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다. id=" + portfolioId));

        // 중복 찜 방지 검증
        if (likeRepository.existsByUserAndPortfolio(user, portfolio)) {
            throw new IllegalStateException("이미 좋아요를 누른 포트폴리오입니다.");
        }

        Like like = Like.builder()
                .user(user)
                .portfolio(portfolio)
                .build();

        likeRepository.save(like);
    }

    // [기능 2] 좋아요 취소
    @Transactional
    public void removeLike(Long userId, Long portfolioId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + userId));
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다. id=" + portfolioId));

        Like like = likeRepository.findByUserAndPortfolio(user, portfolio)
                .orElseThrow(() -> new IllegalArgumentException("좋아요 내역이 존재하지 않습니다."));

        likeRepository.delete(like);
    }

    // [기능 3] 좋아요 목록 조회
    @Transactional(readOnly = true)
    public List<PortfolioResponse> getMyLikedPortfolios(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + userId));

        List<Like> likes = likeRepository.findAllByUser(user);

        return likes.stream()
                .map(like -> PortfolioResponse.from(like.getPortfolio()))
                .collect(Collectors.toList());
    }
}