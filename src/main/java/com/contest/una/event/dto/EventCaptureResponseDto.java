package com.contest.una.event.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EventCaptureResponseDto {
    private Long sourceImageId;
    private String imageUrl;
    private List<Candidate> candidates;
    private List<String> warnings;

    @Getter
    @Builder
    public static class Candidate {
        private String courseName;      // AI가 읽은 과목명
        private Long userLectureId;     // 내 시간표와 매칭된 항목. 못 찾으면 null(강의 무관)
        private String lectureName;
        private String title;
        private String eventType;
        private String eventDate;       // YYYY-MM-DD, 읽지 못했으면 null
        private String eventTime;       // HH:mm:ss, 미지정이면 null
        private String memo;
        private boolean needsCheck;
        private List<String> checkReasons;
        private boolean duplicate;
    }
}
