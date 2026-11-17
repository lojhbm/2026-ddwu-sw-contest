package com.contest.una.event.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.OffsetDateTime;

@Getter
@Builder
public class EventCreateResponseDto {
    private final Long eventId;
    private final Long sourceNoticeId;
    private final Long userLectureId;
    private final String title;
    private final String eventType;
    private final String eventDate;  // YYYY-MM-DD
    private final String eventTime;  // HH:mm:ss, 미지정이면 null
    private final String memo;
    private final Instant createdAt;
    private final Instant updatedAt;
}