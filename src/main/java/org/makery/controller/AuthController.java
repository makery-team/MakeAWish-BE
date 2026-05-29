package org.makery.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.makery.dto.AuthTokenResponse;
import org.makery.dto.SocialLoginRequest;
import org.makery.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/{provider}")
    public ResponseEntity<AuthTokenResponse> socialLogin(
            @PathVariable("provider") String provider,
            @Valid @RequestBody SocialLoginRequest request) {

        AuthTokenResponse response = authService.socialLogin(provider, request.token());
        return ResponseEntity.ok(response);
    }
}
