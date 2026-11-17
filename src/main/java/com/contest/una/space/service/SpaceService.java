package com.contest.una.space.service;

import com.contest.una.global.exception.InvalidRequestException;
import com.contest.una.global.exception.SpaceNotFoundException;
import com.contest.una.space.domain.CongestionLevel;
import com.contest.una.space.domain.SpaceEntity;
import com.contest.una.space.domain.SpaceVoteEntity;
import com.contest.una.space.dto.SpaceListResponseDto;
import com.contest.una.space.dto.SpaceVoteCreateRequest;
import com.contest.una.space.dto.SpaceVoteCreateResponse;
import com.contest.una.space.repository.SpaceRepository;
import com.contest.una.space.repository.SpaceVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpaceService {
    // 투표 유효 시간(분). 투표 등록의 expiresAt과 목록 조회의 집계 기준이 함께 사용
    public static final int VOTE_WINDOW_MINUTES = 30;
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

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

    @Transactional(readOnly = true)
    public SpaceListResponseDto getSpaces(Long userId, String buildingName, Integer page, Integer size) {
        return getSpaces(userId, buildingName, page, size, Instant.now());
    }

    // 집계 기준 시각(asOf)을 받는 버전. 테스트에서 시각을 고정하기 위해 분리
    @Transactional(readOnly = true)
    public SpaceListResponseDto getSpaces(
            Long userId,
            String buildingName,
            Integer page,
            Integer size,
            Instant asOf
    ) {
        // 1. 요청 값 검증 (실패하면 400 VALIDATION_ERROR)
        int pageNumber = validatePage(page);
        int pageSize = validateSize(size);
        // 건물명이 비어 있으면 필터 없이 전체 조회
        String building = (buildingName == null || buildingName.isBlank())
                ? null
                : buildingName.strip();

        // 2. 시설 목록 (spaceId 오름차순, 페이징)
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id"));
        Page<SpaceEntity> spacePage = (building == null)
                ? spaceRepository.findAll(pageable)
                : spaceRepository.findByBuildingName(building, pageable);

        // 3. 이 페이지 시설들의 유효 투표 (시설별, 사용자별 최신 1표)
        List<Long> spaceIds = spacePage.getContent().stream().map(SpaceEntity::getId).toList();
        Map<Long, Map<Long, SpaceVoteEntity>> latestVotes =
                spaceIds.isEmpty() ? Map.of() : findLatestValidVotes(spaceIds, asOf);

        List<SpaceListResponseDto.SpaceItem> items = spacePage.getContent().stream()
                .map(space -> toItem(space, latestVotes.getOrDefault(space.getId(), Map.of()), userId))
                .toList();

        return SpaceListResponseDto.builder()
                .items(items)
                .page(pageNumber)
                .size(pageSize)
                .totalElements(spacePage.getTotalElements())
                .totalPages(spacePage.getTotalPages())
                .windowMinutes(VOTE_WINDOW_MINUTES)
                .asOf(asOf)
                .build();
    }

    // 유효 기간(투표 시각 + 30분 > asOf) 안의 투표를 시설별, 사용자별로 최신 1표만 남겨 반환
    private Map<Long, Map<Long, SpaceVoteEntity>> findLatestValidVotes(List<Long> spaceIds, Instant asOf) {
        Instant since = asOf.minus(Duration.ofMinutes(VOTE_WINDOW_MINUTES));
        Map<Long, Map<Long, SpaceVoteEntity>> result = new HashMap<>();
        for (SpaceVoteEntity vote : spaceVoteRepository.findBySpaceIdInAndCreatedAtAfter(spaceIds, since)) {
            result.computeIfAbsent(vote.getSpaceId(), id -> new HashMap<>())
                    .merge(vote.getUserId(), vote, SpaceService::newerVote);
        }
        return result;
    }

    // 더 늦게 만든 투표를 선택. 시각이 같으면 id가 큰(나중에 저장된) 투표
    private static SpaceVoteEntity newerVote(SpaceVoteEntity a, SpaceVoteEntity b) {
        int byTime = a.getCreatedAt().compareTo(b.getCreatedAt());
        if (byTime != 0) {
            return byTime > 0 ? a : b;
        }
        return a.getId() >= b.getId() ? a : b;
    }

    private static SpaceListResponseDto.SpaceItem toItem(
            SpaceEntity space,
            Map<Long, SpaceVoteEntity> latestByUser,
            Long userId
    ) {
        int low = 0;
        int medium = 0;
        int high = 0;
        CongestionLevel top = null;       // 유효 투표 중 가장 높은 단계
        Instant latestVoteAt = null;

        for (SpaceVoteEntity vote : latestByUser.values()) {
            CongestionLevel level = CongestionLevel.from(vote.getCongestionLevel()).orElse(null);
            if (level == null) {
                continue;   // 허용되지 않는 값이 저장된 행은 집계에서 제외
            }
            switch (level) {
                case LOW -> low++;
                case MEDIUM -> medium++;
                case HIGH -> high++;
            }
            if (top == null || level.compareTo(top) > 0) {
                top = level;
            }
            if (latestVoteAt == null || vote.getCreatedAt().isAfter(latestVoteAt)) {
                latestVoteAt = vote.getCreatedAt();
            }
        }

        SpaceVoteEntity mine = latestByUser.get(userId);
        SpaceListResponseDto.MyVote myVote = (mine == null)
                ? null
                : SpaceListResponseDto.MyVote.builder()
                  .congestionLevel(mine.getCongestionLevel())
                  .createdAt(mine.getCreatedAt())
                  .build();

        return SpaceListResponseDto.SpaceItem.builder()
                .spaceId(space.getId())
                .name(space.getName())
                .buildingName(space.getBuildingName())
                .congestionLevel(top == null ? CongestionLevel.UNKNOWN : top.getValue())
                .validVoteCount(low + medium + high)
                .distribution(SpaceListResponseDto.Distribution.builder()
                        .low(low)
                        .medium(medium)
                        .high(high)
                        .build())
                .myVote(myVote)
                .latestVoteAt(latestVoteAt)
                .build();
    }

    private static int validatePage(Integer page) {
        if (page == null) {
            return DEFAULT_PAGE;
        }
        if (page < 0) {
            throw new InvalidRequestException("page", "0 이상의 정수를 입력해 주세요.");
        }
        return page;
    }

    private static int validateSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new InvalidRequestException("size", "1부터 100 사이의 정수를 입력해 주세요.");
        }
        return size;
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