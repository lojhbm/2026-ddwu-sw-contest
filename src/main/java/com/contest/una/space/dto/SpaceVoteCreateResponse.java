package com.contest.una.space.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class SpaceVoteCreateResponse {
    private Long voteId;
    private Long spaceId;
    private String congestionLevel;
    private Instant createdAt;
    private Instant expiresAt;  // createdAt + 30분
}