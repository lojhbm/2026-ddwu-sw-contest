package com.contest.una.classroom.controller;

import com.contest.una.classroom.dto.ClassroomAvailabilityRequest;
import com.contest.una.classroom.dto.ClassroomAvailabilityResponse;
import com.contest.una.classroom.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/classroom")
public class ClassroomController {
    private final ClassroomService classroomService;

    // 명세에 따라 GET. 요청 값은 본문(JSON) 또는 쿼리 파라미터로 받음
    // (브라우저 fetch는 GET에 본문을 실을 수 없어 웹 클라이언트는 쿼리 파라미터를 사용)
    @GetMapping("/availability")
    public ResponseEntity<ClassroomAvailabilityResponse> getAvailability(
            @AuthenticationPrincipal Long userId,
            @RequestBody(required = false) ClassroomAvailabilityRequest body,
            @ModelAttribute ClassroomAvailabilityRequest query
    ) {
        ClassroomAvailabilityRequest request = body != null ? body : query;
        return ResponseEntity.ok(classroomService.getAvailability(userId, request));
    }
}