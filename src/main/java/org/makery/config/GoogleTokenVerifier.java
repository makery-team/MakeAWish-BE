package org.makery.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(@Value("${app.auth.google.web-client-id}") String webClientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                // 모바일 ID가 아닌 백엔드용 Web Client ID로 대상(Audience) 검증
                .setAudience(Collections.singletonList(webClientId))
                .build();
    }

    public GoogleIdToken.Payload verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new IllegalArgumentException("유효하지 않은 Google ID Token입니다.");
            }
            return idToken.getPayload();
        } catch (Exception e) {
            throw new SecurityException("Google 토큰 검증에 실패했습니다. (위변조 또는 만료)", e);
        }
    }
}
