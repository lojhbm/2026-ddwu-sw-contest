package com.contest.una.notice.ai;

import com.contest.una.global.exception.AiAnalysisFailedException;
import com.contest.una.global.exception.AiAnalysisTimeoutException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gemini generateContent REST API 호출 담당.
 * - 503/429(및 네트워크 오류)는 같은 모델로 재시도하고, 계속 실패하면 fallback 모델로 넘어간다.
 * - 모델을 찾을 수 없는 404는 재시도 없이 바로 fallback 모델로 넘어간다.
 * - 응답 시간 초과는 재시도하지 않고 AiAnalysisTimeoutException(504)을 던진다.
 * - 그 외 실패는 AiAnalysisFailedException(502)을 던진다.
 */
@Slf4j
@Component
public class GeminiClient {

    private static final int MAX_ATTEMPTS_PER_MODEL = 2;
    private static final int NETWORK_ERROR = -1;
    private static final int LOG_BODY_LIMIT = 500;

    private final GeminiProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GeminiClient(GeminiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(properties.connectTimeoutSeconds()))
                .build();
    }

    /**
     * 이미지 1장과 프롬프트를 보내고, 모델이 만든 JSON 문자열을 그대로 반환한다.
     * JSON 내용 검증은 호출한 쪽(NoticeAiAnalyzer)이 한다.
     */
    public String generateJson(
            String systemInstruction,
            String prompt,
            byte[] image,
            String mimeType,
            Map<String, Object> responseSchema
    ) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            log.error("GEMINI_API_KEY가 설정되지 않았습니다.");
            throw new AiAnalysisFailedException();
        }

        // 이미지 base64 변환이 무거우니 요청 본문은 한 번만 만들어 재시도에도 재사용
        String requestBody = buildRequestBody(systemInstruction, prompt, image, mimeType, responseSchema);
        List<String> models = candidateModels();

        for (int m = 0; m < models.size(); m++) {
            String model = models.get(m);
            boolean hasNextModel = m < models.size() - 1;

            for (int attempt = 1; attempt <= MAX_ATTEMPTS_PER_MODEL; attempt++) {
                Response response = post(model, requestBody);
                int status = response.status();

                if (status == 200) {
                    return extractText(response.body(), model);
                }

                boolean retryable = status == 429 || status == 503 || status == NETWORK_ERROR;

                if (retryable && attempt < MAX_ATTEMPTS_PER_MODEL) {
                    log.warn("Gemini 호출 실패, 같은 모델로 재시도합니다. model={}, status={}, attempt={}/{}",
                            model, status, attempt, MAX_ATTEMPTS_PER_MODEL);
                    sleepBeforeRetry();
                    continue;
                }

                if ((retryable || status == 404) && hasNextModel) {
                    log.warn("Gemini 호출 실패, fallback 모델로 전환합니다. model={}, status={}", model, status);
                    break;
                }

                log.error("Gemini 호출에 실패했습니다. model={}, status={}, body={}",
                        model, status, abbreviate(response.body()));
                throw new AiAnalysisFailedException();
            }
        }

        // 위 반복문은 성공하면 return, 실패하면 throw 하므로 여기까지 오는 경우는 사실상 없음
        throw new AiAnalysisFailedException();
    }

    private List<String> candidateModels() {
        List<String> models = new ArrayList<>();
        models.add(properties.model());

        String fallback = properties.fallbackModel();
        if (fallback != null && !fallback.isBlank() && !fallback.equals(properties.model())) {
            models.add(fallback);
        }
        return models;
    }

    private Response post(String model, String requestBody) {
        // API 키는 URL이 아니라 헤더로 보내서 로그에 남지 않도록 함
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.baseUrl() + "/models/" + model + ":generateContent"))
                .timeout(Duration.ofSeconds(properties.readTimeoutSeconds()))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", properties.apiKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new Response(response.statusCode(), response.body());

        } catch (HttpTimeoutException e) {
            // 연결/응답 시간 초과: 더 기다려도 의미가 없으므로 재시도 없이 504
            log.error("Gemini 응답 시간이 초과되었습니다. model={}", model, e);
            throw new AiAnalysisTimeoutException(e);

        } catch (IOException e) {
            log.warn("Gemini 호출 중 네트워크 오류가 발생했습니다. model={}", model, e);
            return new Response(NETWORK_ERROR, String.valueOf(e.getMessage()));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiAnalysisFailedException(e);
        }
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(properties.retryBackoffMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiAnalysisFailedException(e);
        }
    }

    private String buildRequestBody(
            String systemInstruction,
            String prompt,
            byte[] image,
            String mimeType,
            Map<String, Object> responseSchema
    ) {
        Map<String, Object> inlineData = Map.of(
                "mimeType", mimeType,
                "data", Base64.getEncoder().encodeToString(image)
        );

        Map<String, Object> generationConfig = new LinkedHashMap<>();
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.put("responseSchema", responseSchema);
        if (properties.thinkingLevel() != null && !properties.thinkingLevel().isBlank()) {
            generationConfig.put("thinkingConfig", Map.of("thinkingLevel", properties.thinkingLevel()));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("systemInstruction", Map.of("parts", List.of(Map.of("text", systemInstruction))));
        body.put("contents", List.of(Map.of(
                "role", "user",
                "parts", List.of(
                        Map.of("text", prompt),
                        Map.of("inlineData", inlineData)
                )
        )));
        body.put("generationConfig", generationConfig);

        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            log.error("Gemini 요청 본문을 만들지 못했습니다.", e);
            throw new AiAnalysisFailedException(e);
        }
    }

    // candidates[0].content.parts[*].text 를 이어 붙여 반환 (thought 파트는 제외)
    private String extractText(String responseBody, String model) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidate = root.path("candidates").path(0);

            StringBuilder text = new StringBuilder();
            for (JsonNode part : candidate.path("content").path("parts")) {
                if (part.path("thought").asBoolean(false)) {
                    continue;
                }
                if (part.hasNonNull("text")) {
                    text.append(part.get("text").asText());
                }
            }

            if (text.toString().isBlank()) {
                log.error("Gemini 응답에 텍스트가 없습니다. model={}, blockReason={}, finishReason={}",
                        model,
                        root.path("promptFeedback").path("blockReason").asText(""),
                        candidate.path("finishReason").asText(""));
                throw new AiAnalysisFailedException();
            }

            return text.toString();

        } catch (JsonProcessingException e) {
            log.error("Gemini 응답을 해석하지 못했습니다. model={}, body={}", model, abbreviate(responseBody), e);
            throw new AiAnalysisFailedException(e);
        }
    }

    private static String abbreviate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= LOG_BODY_LIMIT ? text : text.substring(0, LOG_BODY_LIMIT) + "...";
    }

    private record Response(int status, String body) {
    }
}