package com.contest.una.event.repository;

import com.contest.una.event.domain.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<EventEntity, Long> {

    // 본인 소유의 일정만 조회 (없거나 타인의 일정이면 비어 있음)
    Optional<EventEntity> findByIdAndUserId(Long id, Long userId);

    // 기간(startDate~endDate, 양 끝 포함) 안의 본인 일정 조회. userLectureId, eventType은 null이면 조건에서 제외
    // 정렬: 날짜 -> 시각 -> id 오름차순 (MySQL은 오름차순에서 NULL이 먼저라 시각 미지정 일정이 그날 맨 앞에 옴)
    @Query("""
            select e from EventEntity e
            where e.userId = :userId
              and e.eventDate between :startDate and :endDate
              and (:userLectureId is null or e.userLectureId = :userLectureId)
              and (:eventType is null or e.eventType = :eventType)
            order by e.eventDate asc, e.eventTime asc, e.id asc
            """)
    List<EventEntity> findAllInRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("userLectureId") Long userLectureId,
            @Param("eventType") String eventType);

    // 공지가 삭제되면 그 공지를 출처로 한 일정은 유지하고 sourceNoticeId만 null로 변경
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update EventEntity e set e.sourceNoticeId = null where e.sourceNoticeId = :noticeId")
    int clearSourceNotice(@Param("noticeId") Long noticeId);

    // 해당 시간표 항목(timetable.id)에 연결된 일정이 있는지 확인 -> 시간표 항목 삭제 시 409
    boolean existsByUserLectureId(Long userLectureId);

    boolean existsBySourceImageId(Long sourceImageId);

    @Query("""
            select count(e) from EventEntity e
            where e.userId = :userId
              and e.title = :title
              and e.eventDate = :eventDate
              and ((:eventTime is null and e.eventTime is null) or e.eventTime = :eventTime)
              and ((:userLectureId is null and e.userLectureId is null) or e.userLectureId = :userLectureId)
            """)
    long countSame(
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("eventDate") LocalDate eventDate,
            @Param("eventTime") LocalTime eventTime,
            @Param("userLectureId") Long userLectureId);
}