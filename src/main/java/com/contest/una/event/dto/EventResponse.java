package com.contest.una.event.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

// 일정 단건 조회 응답이자 일정 목록 조회의 items[] 요소
@Getter
@Builder
public class EventResponse {
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