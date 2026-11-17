package com.contest.una.notice.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

// application.yml의 gemini.* 설정
@ConfigurationProperties(prefix = "gemini")
public record GeminiProperties(
        String apiKey,
        String model,
        String fallbackModel,        // 비어 있으면 fallback 없이 기본 모델만 사용
        String baseUrl,
        int connectTimeoutSeconds,
        int readTimeoutSeconds,
        String thinkingLevel,        // 비어 있으면 thinkingConfig를 보내지 않음
        long retryBackoffMillis      // 재시도 전 대기 시간 (설정 안 하면 1000)
) {
    public GeminiProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://generativelanguage.googleapis.com/v1beta";
        }
        if (connectTimeoutSeconds <= 0) {
            connectTimeoutSeconds = 5;
        }
        if (readTimeoutSeconds <= 0) {
            readTimeoutSeconds = 30;
        }
        if (retryBackoffMillis <= 0) {
            retryBackoffMillis = 1000;
        }
    }
}