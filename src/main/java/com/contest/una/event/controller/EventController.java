package com.contest.una.event.controller;

import com.contest.una.event.dto.EventCreateRequestDto;
import com.contest.una.event.dto.EventCreateResponseDto;
import com.contest.una.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/event")
public class EventController {
    private final EventService eventService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EventCreateResponseDto> createEvent(
            @AuthenticationPrincipal Long userId,
            @RequestBody EventCreateRequestDto request
    ) {

        EventCreateResponseDto response = eventService.createEvent(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }
}