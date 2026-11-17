package com.contest.una.space.repository;

import com.contest.una.space.domain.SpaceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

// 시설 존재 여부는 기본 제공 existsById, 전체 목록은 기본 제공 findAll(Pageable)로 처리
public interface SpaceRepository extends JpaRepository<SpaceEntity, Long> {

    // 건물명이 정확히 일치하는 시설 목록
    Page<SpaceEntity> findByBuildingName(String buildingName, Pageable pageable);
}