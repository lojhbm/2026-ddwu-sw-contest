package com.contest.una.event.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 필드별 상세 검증은 EventService에서 InvalidRequestException(field, reason)으로 처리
@Getter
@Setter
@NoArgsConstructor
public class EventCreateRequestDto {
    private Long sourceNoticeId;
    private Long sourceImageId;
    private Long userLectureId;
    private String title;
    private String eventType;
    private String eventDate;
    private String eventTime;
    private String memo;
}