package com.contest.una.timetable.repository;

import com.contest.una.timetable.domain.TimetableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TimetableRepository extends JpaRepository<TimetableEntity, Long> {

    // 내 시간표에 요청한 강의 중 하나라도 이미 있으면 true -> 409
    boolean existsByUserIdAndLectureIdIn(Long userId, Collection<Long> lectureIds);

    @Query("""
        select t from TimetableEntity t
        join fetch t.lecture l
        where t.user.id = :userId
          and l.academicYear = :academicYear
          and l.semester = :semester
        """)
    List<TimetableEntity> findAllByUserIdAndTerm(
            @Param("userId") Long userId,
            @Param("academicYear") Integer academicYear,
            @Param("semester") String semester);
}