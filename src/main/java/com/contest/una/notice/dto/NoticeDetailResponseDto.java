package com.contest.una.notice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class NoticeDetailResponseDto {
    private Long noticeId;
    private Long userLectureId;
    private String lectureName;
    private String title;
    private String imageUrl;
    private String summary;

    @Builder.Default
    private List<String> actionItems = new ArrayList<>();

    @Builder.Default
    private List<EventCandidateResponseDto> eventCandidates = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;
}
