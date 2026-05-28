package org.makery.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.makery.service.OAuth2AppService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final OAuth2AppService oAuth2AppService;

    // 💡 [수정됨] 웹 클라이언트 ID 대신 모바일(iOS, 안드로이드) 클라이언트 ID를 환경변수에서 주입받습니다.
    @Value("${spring.security.oauth2.client.registration.google.ios-client-id}")
    private String iosClientId;

    @Value("${spring.security.oauth2.client.registration.google.android-client-id}")
    private String androidClientId;

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> request) {
        String idTokenString = request.get("idToken");

        if (idTokenString == null || idTokenString.isEmpty()) {
            return ResponseEntity.badRequest().body("idToken이 전송되지 않았습니다.");
        }

        try {
            // 1. 구글 제공 라이브러리로 프론트가 보낸 idToken 검증
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    // 💡 [핵심 방어선] 오직 모바일(iOS, 안드로이드) 클라이언트 ID만 수신자(Audience)로 허용합니다.
                    // 이로 인해 웹에서 발급된 토큰은 수신자 불일치로 여기서 즉시 차단(401)됩니다.
                    .setAudience(Arrays.asList(iosClientId, androidClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");

                // 2. 가입/로그인 처리 및 서비스 전용 JWT 발행 로직 호출
                Map<String, String> tokenResponse = oAuth2AppService.processUserLoginOrRegister(email, name);

                // 3. 자체 JWT 발급 결과를 앱으로 JSON 응답 반환
                return ResponseEntity.ok(tokenResponse);
            } else {
                // 검증 실패 시 명확한 에러 메시지 반환
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 구글 토큰이거나 모바일 앱 접근이 아닙니다.");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 내부 인증 오류: " + e.getMessage());
        }
    }
}