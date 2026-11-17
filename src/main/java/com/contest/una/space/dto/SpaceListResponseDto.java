package com.contest.una.space.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

// 시설 목록 및 혼잡도 조회 응답
@Getter
@Builder
public class SpaceListResponseDto {
    private List<SpaceItem> items;     // 결과가 없으면 빈 리스트
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;        // 전체 항목이 0이면 0
    private Integer windowMinutes;     // 집계 유효 기간(분)
    private Instant asOf;              // 집계 기준 서버 시각

    @Getter
    @Builder
    public static class SpaceItem {
        private Long spaceId;
        private String name;
        private String buildingName;
        private String congestionLevel;   // low / medium / high, 유효 투표가 없으면 unknown
        private Integer validVoteCount;   // 유효 투표 수 (사용자별 최신 1표)
        private Distribution distribution;
        private MyVote myVote;            // 본인의 최신 유효 투표, 없으면 null
        private Instant latestVoteAt;     // 최신 유효 투표 시각, 없으면 null
    }

    @Getter
    @Builder
    public static class Distribution {
        private Integer low;
        private Integer medium;
        private Integer high;
    }

    @Getter
    @Builder
    public static class MyVote {
        private String congestionLevel;
        private Instant createdAt;
    }
}