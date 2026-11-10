package com.contest.una.notice.domain;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class EventCandidate {
    private String title;
    private String eventType;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String memo;
}
