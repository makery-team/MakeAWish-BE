package org.makery.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.makery.domain.Language;
import org.makery.domain.User;
import org.makery.dto.UserResponse;
import org.makery.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자가 없습니다."));
    }

    @Transactional
    public User saveOrUpdate(String email, String name, Language language) {
        User user = userRepository.findByEmail(email)
                .map(entity -> entity.update(name, language)) // 엔티티의 인자 2개짜리 update 호출
                .orElse(User.builder()
                        .email(email)
                        .name(name)
                        .language(language)
                        .build());

        return userRepository.save(user);
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Unexpected user"));
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = findById(userId);
        userRepository.delete(user);
    }

    public UserResponse getUserById(Long userId) {
        User user = findById(userId);
        return new UserResponse(user);
    }
}