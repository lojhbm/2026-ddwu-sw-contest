package com.contest.una.timetable.repository;

import com.contest.una.timetable.domain.LectureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface LectureRepository extends JpaRepository<LectureEntity, Long> {

    // 온라인 강의는 강의실이 없으므로 left join fetch (inner join이면 결과에서 빠짐)
    @Query("""
            select l from LectureEntity l
            left join fetch l.classroom
            where l.id in :ids
              and l.academicYear = :academicYear
              and l.semester = :semester
            """)
    List<LectureEntity> findAllWithClassroomByIdInAndTerm(
            @Param("ids") Collection<Long> ids,
            @Param("academicYear") Integer academicYear,
            @Param("semester") String semester);

    // 강의실별로 조회 종료 시각 이후 가장 빠른 수업 시작 시각 (이후 수업이 없는 강의실은 결과에 없음)
    @Query("""
            select l.classroom.id as classroomId, min(l.timeStartsAt) as startsAt
            from LectureEntity l
            where l.classroom.id in :classroomIds
              and l.academicYear = :academicYear
              and l.semester = :semester
              and l.timeWeek = :timeWeek
              and l.timeStartsAt >= :endTime
            group by l.classroom.id
            """)
    List<NextLectureStart> findNextLectureStarts(
            @Param("classroomIds") Collection<Long> classroomIds,
            @Param("academicYear") Integer academicYear,
            @Param("semester") String semester,
            @Param("timeWeek") Integer timeWeek,
            @Param("endTime") LocalTime endTime);

    interface NextLectureStart {
        Long getClassroomId();

        LocalTime getStartsAt();
    }
}