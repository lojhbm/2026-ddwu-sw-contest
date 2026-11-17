package com.contest.una.event.repository;

import com.contest.una.event.domain.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<EventEntity, Long> {

    // 공지가 삭제되면 그 공지를 출처로 한 일정은 유지하고 sourceNoticeId만 null로 변경
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update EventEntity e set e.sourceNoticeId = null where e.sourceNoticeId = :noticeId")
    int clearSourceNotice(@Param("noticeId") Long noticeId);

    // 해당 시간표 항목(timetable.id)에 연결된 일정이 있는지 확인 -> 시간표 항목 삭제 시 409
    boolean existsByUserLectureId(Long userLectureId);
}