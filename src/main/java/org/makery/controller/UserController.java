package org.makery.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.makery.domain.PrincipalDetails;
import org.makery.domain.User;
import org.makery.dto.*;
import org.makery.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 내 프로필 정보 조회 API
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        // PrincipalDetails 내부에 있는 User 엔티티에서 ID 추출
        Long userId = principalDetails.user().getId();

        UserProfileResponse response = userService.getUserProfile(userId);

        return ResponseEntity.ok(response);
    }

    /**
     * 닉네임 중복 확인 API
     * GET /api/users/check-nickname?nickname=케이크천재
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<NicknameCheckResponse> checkNickname(
            @RequestParam("nickname") @NotBlank(message = "닉네임을 입력해주세요.") String nickname) {

        boolean isDuplicate = userService.isNicknameDuplicate(nickname);

        return ResponseEntity.ok(new NicknameCheckResponse(isDuplicate));
    }

    /**
     * 소셜 가입 직후 필수 프로필 설정 API
     * PATCH /api/users/me/init
     */
    @PatchMapping("/me/init")
    public ResponseEntity<Void> initProfile(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid UserProfileInitRequest req) {

        userService.initUserProfile(principalDetails.user().getId(), req);

        return ResponseEntity.ok().build();
    }

    /**
     * 내 정보 수정 API (마이페이지)
     * PATCH /api/users/me
     */
    @PatchMapping("/me")
    public ResponseEntity<Void> updateMyProfile(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody @Valid UserProfileUpdateRequest req) {

        userService.updateMyProfile(principalDetails.user().getId(), req);

        return ResponseEntity.ok().build();
    }
}
