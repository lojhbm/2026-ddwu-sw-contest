package com.contest.una.space.repository;

import com.contest.una.space.domain.SpaceVoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

// 목록·집계 조회용 쿼리는 시설 목록 조회 API에서 추가
public interface SpaceVoteRepository extends JpaRepository<SpaceVoteEntity, Long> {
}