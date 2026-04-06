package org.makery.service;

import lombok.RequiredArgsConstructor;
import org.makery.domain.Tag;
import org.makery.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    public Set<Tag> findByNames(Set<String> names) {
        if (names == null || names.isEmpty()) {
            return new HashSet<>();
        }
        // Repository에서 In 절을 사용하여 한 번의 쿼리로 모든 태그를 가져옵니다.
        return new HashSet<>(tagRepository.findByNameIn(names));
    }

    /**
     * 2. 모든 태그 목록 조회 (프론트엔드 선택창 등에서 사용)
     */
    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    /**
     * 3. (추가 팁) 포트폴리오 등록 시, 없는 태그는 생성하고 있는 태그는 가져오는 로직
     */
    @Transactional
    public Set<Tag> findOrCreateTags(Set<String> names) {
        return names.stream()
                .map(name -> tagRepository.findByName(name)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(name).build())))
                .collect(Collectors.toSet());
    }
}
