package com.contest.una.timetable.repository;

import com.contest.una.timetable.domain.TimetableEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface TimetableRepository extends JpaRepository<TimetableEntity, Long> {

    // 내 시간표에 요청한 강의 중 하나라도 이미 있으면 true -> 409
    boolean existsByUserIdAndLectureIdIn(Long userId, Collection<Long> lectureIds);
}