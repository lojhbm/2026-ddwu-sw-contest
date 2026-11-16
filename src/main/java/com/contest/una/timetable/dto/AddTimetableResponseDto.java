package com.contest.una.timetable.dto;

import com.contest.una.timetable.domain.TimetableEntity;

import java.util.List;

public record AddTimetableResponseDto(
        Integer academicYear,
        String semester,
        String color,
        List<AddedItem> addedItems
) {
    public static AddTimetableResponseDto of(Integer academicYear, String semester, String color,
                                          List<TimetableEntity> timetables) {
        return new AddTimetableResponseDto(
                academicYear,
                semester,
                color,
                timetables.stream().map(AddedItem::from).toList()
        );
    }
}