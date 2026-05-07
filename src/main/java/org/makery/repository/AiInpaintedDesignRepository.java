package org.makery.repository;

import org.makery.domain.AiInpaintedDesign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiInpaintedDesignRepository extends JpaRepository<AiInpaintedDesign, Long> {
}