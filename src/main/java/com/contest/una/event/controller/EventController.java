package com.contest.una.event.controller;

import com.contest.una.event.dto.EventCreateRequestDto;
import com.contest.una.event.dto.EventResponse;
import com.contest.una.event.dto.EventListResponseDto;
import com.contest.una.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/event")
public class EventController {
    private final EventService eventService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EventResponse> createEvent(
            @AuthenticationPrincipal Long userId,
            @RequestBody EventCreateRequestDto request
    ) {

        EventResponse response = eventService.createEvent(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    // 캘린더 일정 목록 조회. 필수 여부와 형식은 서비스에서 검증해 errors[].field가 정확히 내려가도록 함
    @GetMapping
    public ResponseEntity<EventListResponseDto> getEvents(
            @AuthenticationPrincipal Long userId,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "userLectureId", required = false) Long userLectureId,
            @RequestParam(name = "eventType", required = false) String eventType
    ) {
        return ResponseEntity.ok(
                eventService.getEvents(userId, startDate, endDate, userLectureId, eventType)
        );
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEvent(
            @AuthenticationPrincipal Long userId,
            @PathVariable("eventId") Long eventId
    ) {
        return ResponseEntity.ok(eventService.getEvent(userId, eventId));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @AuthenticationPrincipal Long userId,
            @PathVariable("eventId") Long eventId
    ) {
        eventService.deleteEvent(userId, eventId);
        return ResponseEntity.noContent().build();
    }
}