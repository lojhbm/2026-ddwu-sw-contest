package com.contest.una.notice.repository;

import com.contest.una.notice.domain.NoticeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {
    Optional<NoticeEntity> findByIdAndUserId(Long id, Long userId);

    Page<NoticeEntity> findByUserId(Long userId, Pageable pageable);

    // 해당 시간표 항목(timetable.id)에 연결된 공지가 있는지 확인 -> 시간표 항목 삭제 시 409
    boolean existsByUserLectureId(Long userLectureId);
}