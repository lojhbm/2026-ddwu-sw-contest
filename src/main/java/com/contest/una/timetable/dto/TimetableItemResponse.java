package com.contest.una.timetable.dto;

import com.contest.una.timetable.domain.LectureEntity;
import com.contest.una.timetable.domain.TimetableEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

// 시간표 항목 (요일별 수업 행 1개). 조회/등록/수정 응답에서 공통으로 사용
public record TimetableItemResponse(
        Long timetableId,       // timetable.id
        Long lectureId,         // lectures.id
        String name,
        String courseCode,
        String sectionCode,
        Integer academicYear,
        String semester,        // 1 / summer / 2 / winter
        ClassroomResponse classroom, // 온라인 또는 미정이면 null
        Integer dayOfWeek,      // 월요일 1 ~ 일요일 7
        String color,
        @JsonFormat(pattern = "HH:mm:ss") LocalTime startsAt,
        @JsonFormat(pattern = "HH:mm:ss") LocalTime endsAt
) {
    public static TimetableItemResponse from(TimetableEntity timetable) {
        LectureEntity lecture = timetable.getLecture();
        return new TimetableItemResponse(
                timetable.getId(),
                lecture.getId(),
                lecture.getName(),
                lecture.getCourseCode(),
                lecture.getSectionCode(),
                lecture.getAcademicYear(),
                lecture.getSemester(),
                lecture.getClassroom() == null ? null : ClassroomResponse.from(lecture.getClassroom()),
                lecture.getTimeWeek(),
                timetable.getColor(),
                lecture.getTimeStartsAt(),
                lecture.getTimeEndsAt()
        );
    }
}