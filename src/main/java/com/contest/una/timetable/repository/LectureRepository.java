package com.contest.una.timetable.repository;

import com.contest.una.timetable.domain.LectureEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // 요일별 행을 분반(학수번호+분반) 단위로 묶어 페이징. pattern이 null이면 학기 전체
    @Query(value = """
            select l.courseCode as courseCode, l.sectionCode as sectionCode
            from LectureEntity l
            where l.academicYear = :academicYear
              and l.semester = :semester
              and (:pattern is null
                   or lower(replace(l.name, ' ', '')) like :pattern escape '\\'
                   or lower(l.courseCode) like :pattern escape '\\')
            group by l.courseCode, l.sectionCode
            order by min(l.name), l.courseCode, l.sectionCode
            """,
            countQuery = """
            select count(distinct concat(l.courseCode, '|', l.sectionCode))
            from LectureEntity l
            where l.academicYear = :academicYear
              and l.semester = :semester
              and (:pattern is null
                   or lower(replace(l.name, ' ', '')) like :pattern escape '\\'
                   or lower(l.courseCode) like :pattern escape '\\')
            """)
    Page<SectionKey> searchSections(
            @Param("academicYear") Integer academicYear,
            @Param("semester") String semester,
            @Param("pattern") String pattern,
            Pageable pageable);

    // sectionKeys: "학수번호|분반"
    @Query("""
            select l from LectureEntity l
            left join fetch l.classroom
            where l.academicYear = :academicYear
              and l.semester = :semester
              and concat(l.courseCode, '|', l.sectionCode) in :sectionKeys
            order by l.timeWeek, l.timeStartsAt
            """)
    List<LectureEntity> findAllWithClassroomBySections(
            @Param("academicYear") Integer academicYear,
            @Param("semester") String semester,
            @Param("sectionKeys") Collection<String> sectionKeys);

    interface SectionKey {
        String getCourseCode();

        String getSectionCode();
    }

    interface NextLectureStart {
        Long getClassroomId();

        LocalTime getStartsAt();
    }
}