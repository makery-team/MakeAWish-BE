package org.makery.repository;

import org.makery.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByNameIn(Collection<String> names);

    // 단일 이름으로 조회
    Optional<Tag> findByName(String name);
}
