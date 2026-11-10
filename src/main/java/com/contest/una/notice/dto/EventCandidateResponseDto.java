package com.contest.una.notice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventCandidateResponseDto {
    private String title;
    private String eventType;
    private String eventDate;
    private String eventTime;
    private String memo;
}
