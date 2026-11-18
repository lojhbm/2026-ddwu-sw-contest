package com.contest.una.timetable.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;
import java.util.List;

public record LectureSearchResponse(
        Integer academicYear,
        String semester,
        String keyword,
        List<Item> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    // 분반 단위. lectureIds를 시간표 등록에 그대로 전달
    public record Item(
            String courseCode,
            String sectionCode,
            String name,
            List<Long> lectureIds,
            List<Schedule> schedules,
            boolean inMyTimetable
    ) {
    }

    public record Schedule(
            Long lectureId,
            Integer dayOfWeek,
            @JsonFormat(pattern = "HH:mm:ss") LocalTime startsAt,
            @JsonFormat(pattern = "HH:mm:ss") LocalTime endsAt,
            ClassroomResponse classroom
    ) {
    }
}
