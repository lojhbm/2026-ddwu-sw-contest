package com.contest.una.timetable.repository;

import com.contest.una.timetable.domain.LectureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface LectureRepository extends JpaRepository<LectureEntity, Long> {

    // 온라인 강의는 강의실이 없으므로 left join fetch (inner join이면 결과에서 빠짐)
    @Query("select l from LectureEntity l left join fetch l.classroom where l.id in :ids")
    List<LectureEntity> findAllWithClassroomByIdIn(@Param("ids") Collection<Long> ids);
}