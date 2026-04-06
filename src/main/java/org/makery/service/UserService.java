package org.makery.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.makery.domain.Language;
import org.makery.domain.User;
import org.makery.dto.UserResponse;
import org.makery.dto.UserSetupRequest;
import org.makery.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 이메일로 사용자 조회
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호를 다시 확인해주세요."));
    }

    /**
     * ID로 사용자 조회
     */
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("요청하신 정보를 찾을 수 없습니다."));
    }

    /**
     * 소셜 가입 직후 필수 프로필 설정
     */
    @Transactional
    public void updateAdditionalInfo(Long id, UserSetupRequest userSetupRequest) {

        User user = findById(id);

        user.updateProfile(
                userSetupRequest.getNickname(),
                userSetupRequest.getPhoneNumber(),
                userSetupRequest.getLanguage()
        );
    }

    /**
     * 회원 탈퇴 (사용자 삭제)
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = findById(userId);
        userRepository.delete(user);
    }

    /**
     * 마이페이지 등 정보 조회를 위한 DTO 반환
     */
    public UserResponse getUserById(Long userId) {
        User user = findById(userId);
        return new UserResponse(user);
    }
}