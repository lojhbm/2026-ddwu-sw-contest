package com.contest.una.timetable.dto;

import com.contest.una.classroom.domain.ClassroomEntity;

public record ClassroomInfo(
        Long classroomId,
        String buildingName,
        String roomName
) {
    public static ClassroomInfo from(ClassroomEntity classroom) {
        return new ClassroomInfo(
                classroom.getId(),
                classroom.getBuildingName(),
                classroom.getRoomName()
        );
    }
}