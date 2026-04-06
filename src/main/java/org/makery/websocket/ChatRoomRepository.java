package org.makery.websocket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT c FROM ChatRoom c WHERE c.user.id = :userId AND c.other.id = :otherId")
    Optional<ChatRoom> findByUserAndOtherId(Long userId, Long otherId);

    List<ChatRoom> findByUserIdOrOtherId(Long userId, Long otherId);

    Optional<ChatRoom> findByRoomNumber(Long roomNumber);
}

