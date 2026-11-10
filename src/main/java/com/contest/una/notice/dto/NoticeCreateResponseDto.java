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
    private String LectureId;
    private String title;
    private String ImgeUrl;
    private String summary;
    private List<String> acctionItems;
    private List<EventCandidateResponseDto> eventCandidates;
    private List<String> warnings = new ArrayList<>(); // 아직은 빈 배열로 구현, 추후 ai 기능 구현시 보완
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
