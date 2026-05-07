package org.makery.repository;

import org.makery.domain.AiAgentMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiAgentMessageRepository extends JpaRepository<AiAgentMessage, Long> {

    /**
     * 특정 사용자의 전체 AI 대화 내역을 시간순으로 조회
     * (채팅창 이력을 다시 불러올 때 사용)
     */
    List<AiAgentMessage> findByUserIdOrderByCreatedAtAsc(Long userId);

    /**
     * 최근 대화 세션별로 이력을 조회하거나, 특정 개수만큼만 가져올 때 확장 가능
     */
}