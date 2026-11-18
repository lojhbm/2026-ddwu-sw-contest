package com.contest.una.timetable.controller;

import com.contest.una.timetable.dto.LectureSearchResponse;
import com.contest.una.timetable.service.LectureService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/lecture")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    @GetMapping
    public ResponseEntity<LectureSearchResponse> searchLectures(
            @AuthenticationPrincipal Long userId,
            @RequestParam @NotNull @Min(1900) @Max(9999) Integer academicYear,
            @RequestParam @NotNull @Pattern(regexp = "^(1|summer|2|winter)$") String semester,
            @RequestParam(required = false) @Size(max = 100) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(
                lectureService.search(userId, academicYear, semester, keyword, page, size)
        );
    }
}
