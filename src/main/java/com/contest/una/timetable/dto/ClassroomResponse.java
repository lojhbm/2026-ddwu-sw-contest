package com.contest.una.timetable.dto;


 // 온라인/미정인 강의는 TimetableItemResponse.classroom 이 null.
 public record ClassroomResponse(
        Long classroomId,
        String buildingName,
        String roomName
) {
}