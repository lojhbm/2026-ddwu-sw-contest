package com.contest.una.timetable.dto;

import java.util.List;

// 시간표 조회 응답. 결과가 없으면 items 는 빈 리스트.
public record TimetableListResponse(
        Integer academicYear,
        String semester,
        List<TimetableItemResponse> items
) {
    public TimetableListResponse {
        items = (items == null) ? List.of() : items;
    }
}