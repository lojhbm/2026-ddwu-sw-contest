package com.contest.una.notice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class NoticeCreateResponseDto {
    private Long noticeId;
    private String userLectureId;
    private String title;
    private String imageUrl;
    private String summary;
    private List<String> actionItems = new ArrayList<>();
    private List<EventCandidateResponseDto> eventCandidates = new ArrayList<>();
    private List<String> warnings = new ArrayList<>(); // 아직은 빈 배열로 구현, 추후 ai 기능 구현시 보완
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
