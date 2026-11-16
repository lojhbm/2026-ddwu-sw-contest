package com.contest.una.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long validityInMilliseconds;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expire-length}") long validityInMilliseconds
    ) {
        if (validityInMilliseconds <= 0) {
            throw new IllegalArgumentException(
                    "JWT 유효기간은 0보다 커야 합니다."
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
        this.validityInMilliseconds = validityInMilliseconds;
    }

    // 토큰 생성: Long 타입의 userId를 문자열로 변환해 저장
    public String createToken(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "사용자 ID는 필수입니다."
            );
        }

        Date now = new Date();
        Date expiration =
                new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    // 서명과 만료 여부를 검증한 뒤 사용자 ID 반환
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String subject = claims.getSubject();

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(
                    "토큰에 사용자 ID가 없습니다."
            );
        }

        return Long.valueOf(subject);
    }

    // 필요한 곳에서 토큰 유효 여부만 확인할 때 사용
    public boolean validateToken(String token) {
        try {
            getUserIdFromToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 로그인 응답의 expiresIn에 사용할 유효기간(초)
    public Integer getExpiresInSeconds() {
        return Math.toIntExact(validityInMilliseconds / 1000);
    }
}
