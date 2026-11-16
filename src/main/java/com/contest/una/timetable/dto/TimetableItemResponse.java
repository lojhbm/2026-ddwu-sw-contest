package com.contest.una.timetable.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;


//  시간표 항목 (요일별 수업 행 1개)
public record TimetableItemResponse(
        Long userLectureId,   // timetable.id
        Long lectureId,       // lectures.id
        String name,
        String courseCode,
        String sectionCode,
        Integer academicYear,
        String semester,        // 1 / summer / 2 / winter
        ClassroomResponse classroom, // 온라인 또는 미정이면 null
        Integer dayOfWeek,      // 월요일 1 ~ 일요일 7
        @JsonFormat(pattern = "HH:mm:ss") LocalTime startsAt,
        @JsonFormat(pattern = "HH:mm:ss") LocalTime endsAt
) {
}