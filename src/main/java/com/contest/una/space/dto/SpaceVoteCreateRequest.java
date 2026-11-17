package com.contest.una.space.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

// 시설 혼잡도 투표 등록 요청 본문
// 필수 여부와 값 검증은 서비스에서 해 errors[].field가 정확히 내려가도록 함
@Getter
@NoArgsConstructor
public class SpaceVoteCreateRequest {
    private String congestionLevel;  // low / medium / high
}