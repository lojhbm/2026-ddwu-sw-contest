package com.contest.una.notice.ai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// GeminiProperties(gemini.* 설정)를 스프링 빈으로 등록
@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
public class GeminiConfig {
}