package com.contest.una.space.repository;

import com.contest.una.space.domain.SpaceVoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface SpaceVoteRepository extends JpaRepository<SpaceVoteEntity, Long> {

    // since 이후(since 시각은 제외)에 생성된 투표. 지정한 시설들의 유효 기간 안 투표를 한 번에 조회
    // 사용자별 최신 1표 선택은 서비스에서 처리
    List<SpaceVoteEntity> findBySpaceIdInAndCreatedAtAfter(Collection<Long> spaceIds, Instant since);
}