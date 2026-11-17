package com.contest.una.timetable.dto;

import com.contest.una.classroom.domain.ClassroomEntity;

// 온라인/미정인 강의는 TimetableItemResponse.classroom 이 null.
public record ClassroomResponse(
        Long classroomId,
        String buildingName,
        String roomName
) {
    public static ClassroomResponse from(ClassroomEntity classroom) {
        return new ClassroomResponse(
                classroom.getId(),
                classroom.getBuildingName(),
                classroom.getRoomName()
        );
    }
}