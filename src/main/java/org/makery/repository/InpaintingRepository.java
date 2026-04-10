package org.makery.repository;

import org.makery.domain.Inpainting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InpaintingRepository extends JpaRepository<Inpainting, Long> {
    // 💡 JpaRepository를 상속받으면 save(), findById() 등을 공짜로 쓸 수 있어요!
}