package com.contest.una.timetable.controller;

import com.contest.una.timetable.dto.AddTimetableRequestDto;
import com.contest.una.timetable.dto.AddTimetableResponseDto;
import com.contest.una.timetable.service.TimetableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}