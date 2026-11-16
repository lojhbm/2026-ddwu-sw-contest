package com.contest.una.timetable.dto;

import com.contest.una.timetable.domain.LectureEntity;
import com.contest.una.timetable.domain.TimetableEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

public record AddedItem(
        Long userLectureId,
        Long lectureId,
        String name,
        String courseCode,
        String sectionCode,
        Integer academicYear,
        String semester,
        ClassroomInfo classroom,
        Integer dayOfWeek,
        @JsonFormat(pattern = "HH:mm:ss") LocalTime startsAt,
        @JsonFormat(pattern = "HH:mm:ss") LocalTime endsAt
) {
    public static AddedItem from(TimetableEntity timetable) {
        LectureEntity lecture = timetable.getLecture();
        return new AddedItem(
                timetable.getId(),
                lecture.getId(),
                lecture.getName(),
                lecture.getCourseCode(),
                lecture.getSectionCode(),
                lecture.getAcademicYear(),
                lecture.getSemester(),
                lecture.getClassroom() == null ? null : ClassroomInfo.from(lecture.getClassroom()),
                lecture.getTimeWeek(),
                lecture.getTimeStartsAt(),
                lecture.getTimeEndsAt()
        );
    }
}