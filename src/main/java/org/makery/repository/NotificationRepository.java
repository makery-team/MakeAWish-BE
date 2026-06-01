package org.makery.repository;

import org.makery.domain.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 특정 유저의 알림 내역을 페이징 단위로 잘라서 조회합니다.
     */
    Slice<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}