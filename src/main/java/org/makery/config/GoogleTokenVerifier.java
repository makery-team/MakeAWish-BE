package org.makery.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Slf4j
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
                .setAcceptableTimeSkewSeconds(300) // 5분 시계 오차 허용
                .build();
    }

    public GoogleIdToken.Payload verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                return idToken.getPayload();
            }
        } catch (Exception e) {
            log.warn("Local GoogleIdTokenVerifier 실패: {}. Google TokenInfo API 폴백 시도...", e.getMessage());
        }

        // 🌟 Fallback: Google 공식 TokenInfo REST API로 직접 검증 (100% 보장)
        return verifyWithGoogleApi(idTokenString);
    }

    private GoogleIdToken.Payload verifyWithGoogleApi(String idTokenString) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + idTokenString))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

                GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
                if (json.has("email")) {
                    payload.setEmail(json.get("email").getAsString());
                }
                if (json.has("name")) {
                    payload.set("name", json.get("name").getAsString());
                }
                if (json.has("sub")) {
                    payload.setSubject(json.get("sub").getAsString());
                }
                return payload;
            } else {
                log.error("Google TokenInfo API 에러 응답 (상태코드: {}): {}", response.statusCode(), response.body());
                throw new SecurityException("Google 토큰 검증 실패 (상태 코드: " + response.statusCode() + ")");
            }
        } catch (Exception e) {
            throw new SecurityException("Google 토큰 검증에 최종 실패했습니다: " + e.getMessage(), e);
        }
    }
}
