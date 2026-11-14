package com.contest.una.notice.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public record NoticeListItemResponse(
        Long noticeId,
        Long userLectureId,
        String lectureName,
        String title,
        String summary,
        Instant createdAt
) {
}


