package com.contest.una.timetable.dto;

import com.contest.una.timetable.domain.TimetableEntity;
import com.contest.una.timetable.dto.TimetableItemResponse;

import java.util.List;

public record UpdateTimetableColorResponse(
        List<TimetableItemResponse> items
) {
    public static UpdateTimetableColorResponse from(List<TimetableEntity> timetables) {
        return new UpdateTimetableColorResponse(
                timetables.stream().map(TimetableItemResponse::from).toList()
        );
    }
}