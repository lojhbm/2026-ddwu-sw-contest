package com.contest.una.event.ai;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// eventDate가 null이면 사용자가 날짜를 입력해야 저장 가능
public record AssignmentCandidate(
        String courseName,
        String title,
        String eventType,
        LocalDate eventDate,
        LocalTime eventTime,
        String memo,
        List<String> checkReasons
) {
}
