package com.contest.una.space.domain;

import java.util.Optional;

// 혼잡도 단계. 선언 순서가 곧 높낮이 순서다. (LOW < MEDIUM < HIGH)
// 시설 대표 혼잡도는 유효 투표 중 가장 높은 단계로 정하므로 compareTo로 비교한다.
public enum CongestionLevel {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    // 투표가 하나도 없는 시설의 대표 혼잡도 (투표 값으로는 쓸 수 없음)
    public static final String UNKNOWN = "unknown";

    private final String value;

    CongestionLevel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // "low" / "medium" / "high"만 인정. 대소문자가 다르거나 그 외 값이면 비어 있음
    public static Optional<CongestionLevel> from(String value) {
        for (CongestionLevel level : values()) {
            if (level.value.equals(value)) {
                return Optional.of(level);
            }
        }
        return Optional.empty();
    }
}