package org.makery.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    // Web, Android, iOS 모든 클라이언트 ID를 기본 허용 목록으로 등록
    private static final List<String> KNOWN_CLIENT_IDS = List.of(
            "106131390766-mnqk6vkbs4n33s2tt63om1860e6cgaau.apps.googleusercontent.com", // Web
            "106131390766-5tiajbml0itkiohc580sm5mc3t3tiahb.apps.googleusercontent.com", // Android
            "106131390766-vmcvo280rnguao23e9bkmo76d4fnd850.apps.googleusercontent.com"  // iOS
    );

    public GoogleTokenVerifier(@Value("${app.auth.google.web-client-id:}") String webClientId) {
        Set<String> audiences = new HashSet<>(KNOWN_CLIENT_IDS);
        if (webClientId != null && !webClientId.isBlank() && !webClientId.startsWith("${")) {
            audiences.add(webClientId.trim());
        }

        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(audiences)
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
            throw new SecurityException("Google 토큰 검증에 실패했습니다. (위변조 또는 만료): " + e.getMessage(), e);
        }
    }
}
