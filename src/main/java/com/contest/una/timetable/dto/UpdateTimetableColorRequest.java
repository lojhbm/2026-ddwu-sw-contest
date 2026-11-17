package com.contest.una.timetable.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateTimetableColorRequest(
        @NotBlank(message = "color는 필수입니다.")
        String color

) {
}