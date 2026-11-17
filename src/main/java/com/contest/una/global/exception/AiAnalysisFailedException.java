package com.contest.una.global.exception;

// Gemini 호출이 재시도·fallback 후에도 실패했거나, 응답을 사용할 수 없는 경우 -> 502
public class AiAnalysisFailedException extends RuntimeException {

    public AiAnalysisFailedException() {
        super("AI 분석에 실패했습니다. 잠시 후 다시 시도해 주세요.");
    }

    public AiAnalysisFailedException(Throwable cause) {
        super("AI 분석에 실패했습니다. 잠시 후 다시 시도해 주세요.", cause);
    }
}