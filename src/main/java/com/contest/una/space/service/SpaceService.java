package com.contest.una.space.service;

import com.contest.una.global.exception.InvalidRequestException;
import com.contest.una.global.exception.SpaceNotFoundException;
import com.contest.una.space.domain.CongestionLevel;
import com.contest.una.space.domain.SpaceVoteEntity;
import com.contest.una.space.dto.SpaceVoteCreateRequest;
import com.contest.una.space.dto.SpaceVoteCreateResponse;
import com.contest.una.space.repository.SpaceRepository;
import com.contest.una.space.repository.SpaceVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SpaceService {
    // 투표 유효 시간(분). 투표 등록의 expiresAt과 목록 조회의 집계 기준이 함께 사용
    public static final int VOTE_WINDOW_MINUTES = 30;

    private final SpaceRepository spaceRepository;
    private final SpaceVoteRepository spaceVoteRepository;

    @Transactional
    public SpaceVoteCreateResponse createVote(
            Long userId,
            Long spaceId,
            SpaceVoteCreateRequest request
    ) {
        // 1. 요청 값 검증 (실패하면 400 VALIDATION_ERROR)
        if (spaceId == null || spaceId <= 0) {
            throw new InvalidRequestException("spaceId", "양의 정수 ID를 입력해 주세요.");
        }
        CongestionLevel level = validateCongestionLevel(request.getCongestionLevel());

        // 2. 시설 존재 여부 확인 (없으면 404 SPACE_NOT_FOUND)
        if (!spaceRepository.existsById(spaceId)) {
            throw new SpaceNotFoundException();
        }

        // 3. 투표 저장. 재투표도 새 행으로 저장하고, 집계 시 사용자별 최신 1표만 사용
        SpaceVoteEntity saved = spaceVoteRepository.save(
                SpaceVoteEntity.builder()
                        .userId(userId)
                        .spaceId(spaceId)
                        .congestionLevel(level.getValue())
                        .build()
        );

        return SpaceVoteCreateResponse.builder()
                .voteId(saved.getId())
                .spaceId(saved.getSpaceId())
                .congestionLevel(saved.getCongestionLevel())
                .createdAt(saved.getCreatedAt())
                .expiresAt(saved.getCreatedAt().plus(Duration.ofMinutes(VOTE_WINDOW_MINUTES)))
                .build();
    }

    private static CongestionLevel validateCongestionLevel(String congestionLevel) {
        if (congestionLevel == null) {
            throw new InvalidRequestException("congestionLevel", "필수 값입니다.");
        }
        return CongestionLevel.from(congestionLevel)
                .orElseThrow(() -> new InvalidRequestException(
                        "congestionLevel",
                        "low, medium, high 중 하나여야 합니다."
                ));
    }
}