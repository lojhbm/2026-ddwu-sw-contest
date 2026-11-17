package com.contest.una.event.repository;

import com.contest.una.event.domain.CaptureImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CaptureImageRepository extends JpaRepository<CaptureImageEntity, Long> {

    Optional<CaptureImageEntity> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    // 분석 후 일정으로 저장되지 않은 이미지
    @Query("""
            select c from CaptureImageEntity c
            where c.createdAt < :cutoff
              and not exists (select 1 from EventEntity e where e.sourceImageId = c.id)
            """)
    List<CaptureImageEntity> findUnusedCreatedBefore(@Param("cutoff") Instant cutoff);
}
