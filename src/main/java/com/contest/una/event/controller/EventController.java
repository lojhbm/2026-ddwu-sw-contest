package com.contest.una.event.controller;

import com.contest.una.event.dto.EventCaptureRequestDto;
import com.contest.una.event.dto.EventCaptureResponseDto;
import com.contest.una.event.dto.EventCompletionRequestDto;
import com.contest.una.event.dto.EventCreateRequestDto;
import com.contest.una.event.dto.EventResponse;
import com.contest.una.event.dto.EventListResponseDto;
import com.contest.una.event.dto.EventUpdateRequestDto;
import com.contest.una.event.service.EventCaptureService;
import com.contest.una.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/event")
public class EventController {
    private final EventService eventService;
    private final EventCaptureService eventCaptureService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EventResponse> createEvent(
            @AuthenticationPrincipal Long userId,
            @RequestBody EventCreateRequestDto request
    ) {

        EventResponse response = eventService.createEvent(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping(value = "/capture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventCaptureResponseDto> captureAssignments(
            @AuthenticationPrincipal Long userId,
            @ModelAttribute EventCaptureRequestDto request
    ) {
        return ResponseEntity.ok(eventCaptureService.capture(userId, request));
    }

    @GetMapping("/capture/{imageId}/image")
    public ResponseEntity<Resource> getCaptureImage(
            @AuthenticationPrincipal Long userId,
            @PathVariable("imageId") Long imageId
    ) {
        return imageResponse(eventCaptureService.getCaptureImage(userId, imageId));
    }

    @GetMapping("/{eventId}/image")
    public ResponseEntity<Resource> getEventImage(
            @AuthenticationPrincipal Long userId,
            @PathVariable("eventId") Long eventId
    ) {
        return imageResponse(eventService.getEventImage(userId, eventId));
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

    @PutMapping(value = "/{eventId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EventResponse> updateEvent(
            @AuthenticationPrincipal Long userId,
            @PathVariable("eventId") Long eventId,
            @RequestBody EventUpdateRequestDto request
    ) {
        return ResponseEntity.ok(eventService.updateEvent(userId, eventId, request));
    }

    @PatchMapping(value = "/{eventId}/completion", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EventResponse> changeCompletion(
            @AuthenticationPrincipal Long userId,
            @PathVariable("eventId") Long eventId,
            @RequestBody EventCompletionRequestDto request
    ) {
        return ResponseEntity.ok(eventService.changeCompletion(userId, eventId, request));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @AuthenticationPrincipal Long userId,
            @PathVariable("eventId") Long eventId
    ) {
        eventService.deleteEvent(userId, eventId);
        return ResponseEntity.noContent().build();
    }

    private static ResponseEntity<Resource> imageResponse(Resource image) {
        MediaType mediaType = MediaTypeFactory.getMediaType(image)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.empty().cachePrivate())
                .body(image);
    }
}