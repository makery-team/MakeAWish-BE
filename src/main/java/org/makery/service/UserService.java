package org.makery.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.makery.domain.*;
import org.makery.dto.*;
import org.makery.repository.RefreshTokenRepository;
import org.makery.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

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

        return userRepository.existsByNickname(nickname.trim());
    }

    @Transactional
    public void initUserProfile(Long userId, UserProfileInitRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

        // 1. 닉네임 중복 체크 (본인 기존 닉네임과 다른 경우에만 검사)
        if (req.nickname() != null && !req.nickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(req.nickname())) {
                throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
            }
        }

        // 2. 기본 프로필 정보 업데이트
        user.updateProfile(req.nickname(), req.phoneNumber(), req.language());

        // 3. 사장님 앱 가입 요청인 경우 (isSeller == true)
        if (req.checkIsSeller()) {

            // 이미 SellerProfile을 보유하고 있는지 체크 (중복 생성 방지)
            if (user.getSellerProfile() == null) {

                // A. SellerProfile 신규 생성
                SellerProfile sellerProfile = SellerProfile.builder()
                        .businessNo(null)
                        .bankAccount(null)
                        .status(SellerStatus.VERIFIED)
                        .build();

                // B. 기본 매장(Store) 신규 생성
                Store defaultStore = Store.builder()
                        .name(req.nickname())
                        .phone(req.phoneNumber())
                        .description(null)
                        .address(null)
                        .hours(null)
                        .notice(null)
                        .cautionNotice(null)
                        .latitude(null)
                        .longitude(null)
                        .rating(0.0)
                        .reviewCount(0)
                        .build();

                // C. 연관관계 3종 매핑
                sellerProfile.addStore(defaultStore);
                user.registerAsSeller(sellerProfile);

            } else {
                // 이미 사장님 프로필이 존재하는 경우, 권한만 ROLE_SELLER 확인/보장
                user.assignRole(UserRole.ROLE_SELLER);
            }

        } else {
            // 일반 구매자 회원인 경우 ROLE_USER 부여
            user.assignRole(UserRole.ROLE_USER);
        }
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

    /**
     * 회원 탈퇴 처리
     */
    @Transactional
    public void withdraw(User user) {
        // 1. 유저 조회
        User targetUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + user.getId()));

        // 2. 발급된 Refresh Token만 삭제 (재로그인 방지)
        refreshTokenRepository.deleteByUserId(targetUser.getId());

        // 3. 유저 정보 익명화/비활성화 (Dirty Checking으로 자동 DB UPDATE)
        targetUser.withdraw();
    }
}