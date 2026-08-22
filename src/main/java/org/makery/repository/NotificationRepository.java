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

    /**
     * 특정 유저의 미확인(isRead = false) 알림 개수를 카운트합니다.
     */
    long countByUserIdAndIsReadFalse(Long userId);

    /**
     * 특정 유저의 모든 미확인 알림 목록을 조회합니다.
     */
    java.util.List<Notification> findAllByUserIdAndIsReadFalse(Long userId);
}