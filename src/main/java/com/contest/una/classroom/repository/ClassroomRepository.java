package com.contest.una.classroom.repository;

import com.contest.una.classroom.domain.ClassroomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface ClassroomRepository extends JpaRepository<ClassroomEntity, Long> {

    // 해당 건물명의 강의실이 하나라도 있는지 확인 -> 없으면 404 BUILDING_NOT_FOUND
    boolean existsByBuildingName(String buildingName);

    // 같은 연도·학기·요일에서 [startTime, endTime) 구간과 겹치는 수업이 없는 강의실.
    // 겹침 조건: 수업 시작 < 조회 종료 AND 수업 종료 > 조회 시작 (맞닿는 시각은 겹치지 않음)
    @Query("""
            select c from ClassroomEntity c
            where not exists (
                select l.id from LectureEntity l
                where l.classroom.id = c.id
                  and l.academicYear = :academicYear
                  and l.semester = :semester
                  and l.timeWeek = :timeWeek
                  and l.timeStartsAt < :endTime
                  and l.timeEndsAt > :startTime
            )
            order by c.buildingName asc, c.roomName asc
            """)
    List<ClassroomEntity> findAvailable(
            @Param("academicYear") Integer academicYear,
            @Param("semester") String semester,
            @Param("timeWeek") Integer timeWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);
}