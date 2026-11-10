package com.contest.una.notice.controller;

import com.contest.una.notice.dto.NoticeCreateRequestDto;
import com.contest.una.notice.dto.NoticeCreateResponseDto;
import com.contest.una.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notice")
public class NoticeController {
    private final NoticeService noticeService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NoticeCreateResponseDto> createNotice(
             @ModelAttribute NoticeCreateRequestDto request) {
        // 이미지가 포함된 multipart/form-data 요청이므로 @ModelAttribute를 사용

        Long userId = 1L; // Todo: 추후 회원 기능 개발 시 액세스 토큰을 통한 userId 조회 기능 구현

        NoticeCreateResponseDto response =
                noticeService.createNotice(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }
}