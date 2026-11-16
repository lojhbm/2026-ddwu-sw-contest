package com.contest.una.timetable.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

public record AddTimetableRequestDto(
        @NotNull @Min(1900) @Max(9999)
        Integer academicYear,

        @NotNull @Pattern(regexp = "1|2|summer|winter")
        String semester,

        @NotEmpty @UniqueElements
        List<@NotNull Long> lectureIds
) {
}