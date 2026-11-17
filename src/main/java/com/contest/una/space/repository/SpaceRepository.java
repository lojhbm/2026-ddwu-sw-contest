package com.contest.una.space.repository;

import com.contest.una.space.domain.SpaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

// 시설 존재 여부는 기본 제공 existsById로 확인
public interface SpaceRepository extends JpaRepository<SpaceEntity, Long> {
}