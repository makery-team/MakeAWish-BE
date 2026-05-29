package org.makery.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.makery.domain.PrincipalDetails;
import org.makery.domain.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class TokenProvider {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(User user) {
        return makeToken(
                new Date(System.currentTimeMillis() + jwtProperties.accessExpiration()),
                user
        );
    }

    public String createRefreshToken(User user) {
        return makeToken(
                new Date(System.currentTimeMillis() + jwtProperties.refreshExpiration()),
                user
        );
    }

    private String makeToken(Date expiry, User user) {
        Date now = new Date();

        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .issuer(jwtProperties.issuer())
                .issuedAt(now)
                .expiration(expiry)
                .subject(user.getEmail())
                .claim("id", user.getId())
                .claim("role", user.getUserRole().name())
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);

        String role = claims.get("role", String.class);
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority(role != null ? role : "ROLE_USER")
        );

        User user = User.builder()
                .id(claims.get("id", Long.class))
                .email(claims.getSubject())
                .build();

        PrincipalDetails principalDetails = new PrincipalDetails(user);

        return new UsernamePasswordAuthenticationToken(
                principalDetails,
                token,
                authorities);
    }

    public Long getUserId(String token) {
        Claims claims = getClaims(token);
        return claims.get("id", Long.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
