package com.contest.una.event.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.OffsetDateTime;

@Getter
@Builder
public class EventCreateResponseDto {
    private Long eventId;
    private Long sourceNoticeId;
    private Long userLectureId;
    private String title;
    private String eventType;
    private String eventDate;  // YYYY-MM-DD
    private String eventTime;  // HH:mm:ss, 미지정이면 null
    private String memo;
    private Instant createdAt;
    private Instant updatedAt;
}