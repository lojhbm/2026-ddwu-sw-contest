package com.contest.una.classroom.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 빈 강의실 목록 조회 응답
@Getter
@Builder
public class ClassroomAvailabilityResponse {
    private Integer academicYear;
    private String semester;
    private Integer timeWeek;
    private String startTime;              // HH:mm
    private String endTime;                // HH:mm
    private Integer totalCount;            // 조회된 빈 강의실 수
    private List<BuildingItem> buildings;  // 결과가 없으면 빈 리스트

    @Getter
    @Builder
    public static class BuildingItem {
        private String buildingName;
        private Integer classroomCount;    // 해당 건물의 빈 강의실 수
        private List<ClassroomItem> classrooms;
    }

    @Getter
    @Builder
    public static class ClassroomItem {
        private long classroomId;              // classrooms.id (명세상 문자열)
        private String roomName;                 // classrooms.room_name
        private String nextLectureStartTime;     // HH:mm, 이후 수업이 없으면 null
    }
}