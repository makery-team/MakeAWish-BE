package org.makery.repository;

import org.makery.domain.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    Optional<DeviceToken> findByToken(String token);
    List<DeviceToken> findAllByUserId(Long userId);
    void deleteByToken(String token);
    void deleteAllByUserId(Long userId);
}
