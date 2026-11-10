package com.contest.una.notice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EventCandidateResponseDto {
    private String title;
    private String eventType;
    private String eventDate;
    private String eventTime;
    private String memo;
}
