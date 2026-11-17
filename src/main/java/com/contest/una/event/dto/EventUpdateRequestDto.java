package com.contest.una.event.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 전체 교체 방식. 보내지 않은 선택 값은 null로 저장
@Getter
@Setter
@NoArgsConstructor
public class EventUpdateRequestDto {
    private Long userLectureId;
    private String title;
    private String eventType;
    private String eventDate;
    private String eventTime;
    private String memo;
}
