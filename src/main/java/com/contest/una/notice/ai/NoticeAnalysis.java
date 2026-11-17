package com.contest.una.notice.ai;

import com.contest.una.notice.domain.EventCandidate;

import java.util.List;

// 서버 검증을 통과한 AI 분석 결과
public record NoticeAnalysis(
        String title,
        String summary,
        List<String> actionItems,
        List<EventCandidate> eventCandidates,
        List<String> warnings
) {
}