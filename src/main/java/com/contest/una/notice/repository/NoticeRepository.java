package com.contest.una.notice.repository;

import com.contest.una.notice.domain.NoticeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {
    Optional<NoticeEntity> findByIdAndUserId(Long id, Long userId);

    Page<NoticeEntity> findByUserId(Long userId, Pageable pageable);
}
