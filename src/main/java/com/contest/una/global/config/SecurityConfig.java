package com.contest.una.global.config;

import com.contest.una.global.jwt.JwtAuthentication;
import com.contest.una.global.jwt.JwtTokenProvider;
import com.contest.una.global.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtTokenProvider jwtTokenProvider,
            ObjectMapper objectMapper
    ) throws Exception {

        http
                // 쿠키가 아닌 Authorization 헤더로 토큰을 전달하는 구조
                .csrf(csrf -> csrf.disable())

                // 기본 폼 로그인과 HTTP Basic 인증 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 세션을 사용하지 않음
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // 인증 실패 요청을 세션 등에 저장하지 않음
                .requestCache(cache -> cache.disable())

                // 인증 및 권한 오류를 공통 응답 형식으로 반환
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                (request, response, authException) ->
                                        writeErrorResponse(
                                                response,
                                                objectMapper,
                                                HttpServletResponse.SC_UNAUTHORIZED,
                                                "UNAUTHORIZED",
                                                "유효한 인증 정보가 필요합니다."
                                        )
                        )
                        .accessDeniedHandler(
                                (request, response, accessDeniedException) ->
                                        writeErrorResponse(
                                                response,
                                                objectMapper,
                                                HttpServletResponse.SC_FORBIDDEN,
                                                "FORBIDDEN",
                                                "접근 권한이 없습니다."
                                        )
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        // 회원가입과 로그인은 인증 없이 접근 가능
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/users/signup",
                                "/api/v1/users/login"
                        ).permitAll()

                        // Swagger 문서 접근 허용
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // 나머지 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // JWT 인증 필터 등록
                .addFilterBefore(
                        new JwtAuthentication(
                                jwtTokenProvider,
                                objectMapper
                        ),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    private void writeErrorResponse(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            int status,
            String code,
            String message
    ) throws IOException {

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ErrorResponse errorResponse = new ErrorResponse(
                code,
                message,
                List.of()
        );

        objectMapper.writeValue(
                response.getWriter(),
                errorResponse
        );
    }
}
