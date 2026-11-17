package com.contest.una.classroom.controller;

import com.contest.una.classroom.dto.ClassroomAvailabilityRequest;
import com.contest.una.classroom.dto.ClassroomAvailabilityResponse;
import com.contest.una.classroom.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/classroom")
public class ClassroomController {
    private final ClassroomService classroomService;

    // 명세에 따라 GET이지만 요청 값은 본문(JSON)으로 받음
    @GetMapping("/availability")
    public ResponseEntity<ClassroomAvailabilityResponse> getAvailability(
            @AuthenticationPrincipal Long userId,
            @RequestBody ClassroomAvailabilityRequest request
    ) {
        return ResponseEntity.ok(classroomService.getAvailability(userId, request));
    }
}