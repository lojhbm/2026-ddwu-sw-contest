package com.contest.una.global.jwt;

import com.contest.una.global.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthentication extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        // 토큰이 없는 경우 인증 정보를 추가하지 않고 다음 필터로 진행
        if (authorizationHeader == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Bearer 인증 형식 확인
        if (!authorizationHeader.regionMatches(
                true, 0, "Bearer ", 0, 7
        )) {
            sendUnauthorizedResponse(response);
            return;
        }

        String token = authorizationHeader.substring(7);

        if (token.isBlank()) {
            sendUnauthorizedResponse(response);
            return;
        }

        final Long userId;

        try {
            // 토큰 검증 및 Long 타입의 사용자 ID 추출
            userId = jwtTokenProvider.getUserIdFromToken(token);
        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
            sendUnauthorizedResponse(response);
            return;
        }

        // principal에 Long 타입의 userId 저장
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of()
                );

        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        filterChain.doFilter(request, response);
    }

    // 공통 ErrorResponse 형식으로 인증 실패 응답
    private void sendUnauthorizedResponse(
            HttpServletResponse response
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ErrorResponse errorResponse = new ErrorResponse(
                "UNAUTHORIZED",
                "유효한 인증 정보가 필요합니다.",
                List.of()
        );

        objectMapper.writeValue(
                response.getWriter(),
                errorResponse
        );
    }
}
