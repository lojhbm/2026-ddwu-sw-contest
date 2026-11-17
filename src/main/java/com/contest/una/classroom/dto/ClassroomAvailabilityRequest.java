package com.contest.una.classroom.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

// 빈 강의실 목록 조회 요청 본문 (GET /api/v1/classroom/availability)
// 필수 여부와 형식은 서비스에서 검증해 errors[].field가 정확히 내려가도록 함
@Getter
@NoArgsConstructor
public class ClassroomAvailabilityRequest {
    private Integer academicYear;          // 개설 연도 (lectures.academic_year 기준)
    private String semester;               // "1", "summer", "2", "winter"
    private Integer timeWeek;              // 요일. 월요일 1 ~ 일요일 7
    private String startTime;              // 공강 시작 시각 HH:mm
    private String endTime;                // 공강 종료 시각 HH:mm (시작 시각보다 늦어야 함)
    private String previousBuildingName;   // 직전 수업 건물명 (선택)
    private String nextBuildingName;       // 직후 수업 건물명 (선택)
}