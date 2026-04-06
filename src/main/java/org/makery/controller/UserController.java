package org.makery.controller;

import lombok.RequiredArgsConstructor;
import org.makery.domain.User;
import org.makery.dto.UserSetupRequest;
import org.makery.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 소셜 가입 직후 필수 프로필 설정
     */
    @PatchMapping("/setup")
    public ResponseEntity<String> setupProfile(@AuthenticationPrincipal User user,
                                               @RequestBody UserSetupRequest userSetupRequest) {

        userService.updateAdditionalInfo(user.getId(), userSetupRequest);
        return ResponseEntity.ok("프로필 설정이 완료되었습니다.");
    }
}
