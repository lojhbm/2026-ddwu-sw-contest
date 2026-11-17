package com.contest.una.global.exception;

// Gemini 응답을 제한 시간 안에 받지 못한 경우 -> 504
public class AiAnalysisTimeoutException extends RuntimeException {

    public AiAnalysisTimeoutException() {
        super("AI 분석 시간이 초과되었습니다. 잠시 후 다시 시도해 주세요.");
    }

    public AiAnalysisTimeoutException(Throwable cause) {
        super("AI 분석 시간이 초과되었습니다. 잠시 후 다시 시도해 주세요.", cause);
    }
}