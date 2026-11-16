package com.contest.una.timetable.controller;

import com.contest.una.timetable.dto.AddTimetableRequestDto;
import com.contest.una.timetable.dto.AddTimetableResponseDto;
import com.contest.una.timetable.dto.TimetableListResponse;
import com.contest.una.timetable.service.TimetableService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    @PostMapping
    public ResponseEntity<AddTimetableResponseDto> addTimetable(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AddTimetableRequestDto request
    ) {
        AddTimetableResponseDto response =
                timetableService.addTimetable(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<TimetableListResponse> getTimetable(
            @AuthenticationPrincipal Long userId,
            @RequestParam @NotNull @Min(1900) @Max(9999) Integer academicYear,
            @RequestParam @NotNull @Pattern(regexp = "^(1|summer|2|winter)$")  String semester)
    {

        return ResponseEntity.ok(timetableService.getTimetable(userId, academicYear, semester));
    }
}