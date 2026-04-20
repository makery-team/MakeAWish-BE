package org.makery.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.makery.domain.Language;
import org.makery.domain.User;
import org.makery.domain.UserRole;
import org.makery.dto.*;
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

    /**
     * 사용자 ID로 프로필 정보 조회
     */
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. ID: " + userId));

        return UserProfileResponse.from(user);
    }

    @Transactional(readOnly = true)
    public boolean isNicknameDuplicate(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }
        return userRepository.existsByNickname(nickname);
    }

    @Transactional
    public void initUserProfile(Long userId, UserProfileInitRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 1. 닉네임 중복 체크
        if (userRepository.existsByNickname(req.nickname())) {
            throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
        }

        // 2. 프로필 정보 업데이트
        user.updateProfile(req.nickname(), req.phoneNumber(), req.language());

        // 3. 권한 승격 (GUEST -> ROLE_USER)
        // 소셜 로그인 직후 'GUEST' 권한이었던 사용자를 실제 서비스 이용이 가능한 'ROLE_USER'로 변경합니다.
        user.assignRole(UserRole.ROLE_USER);
    }

    @Transactional
    public void updateMyProfile(Long userId, UserProfileUpdateRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 💡 닉네임을 변경하는 경우에만 중복 체크 수행
        if (!user.getNickname().equals(req.nickname())) {
            if (userRepository.existsByNickname(req.nickname())) {
                throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
            }
        }

        // 비즈니스 메서드를 호출하여 정보 수정
        user.updateProfile(req.nickname(), req.phoneNumber(), req.language());
    }
}