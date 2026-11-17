package com.contest.una.space.controller;

import com.contest.una.space.dto.SpaceListResponseDto;
import com.contest.una.space.dto.SpaceVoteCreateRequest;
import com.contest.una.space.dto.SpaceVoteCreateResponse;
import com.contest.una.space.service.SpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/space")
public class SpaceController {
    private final SpaceService spaceService;

    @PostMapping(value = "/{spaceId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SpaceVoteCreateResponse> createVote(
            @AuthenticationPrincipal Long userId,
            @PathVariable("spaceId") Long spaceId,
            @RequestBody SpaceVoteCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(spaceService.createVote(userId, spaceId, request));
    }

    // 시설 목록 및 혼잡도 조회. page, size 범위는 서비스에서 검증해 errors[].field가 정확히 내려가도록 함
    @GetMapping
    public ResponseEntity<SpaceListResponseDto> getSpaces(
            @AuthenticationPrincipal Long userId,
            @RequestParam(name = "buildingName", required = false) String buildingName,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size
    ) {
        return ResponseEntity.ok(spaceService.getSpaces(userId, buildingName, page, size));
    }
}