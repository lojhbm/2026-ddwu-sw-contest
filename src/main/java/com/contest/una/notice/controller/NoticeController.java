package com.contest.una.notice.controller;

import com.contest.una.notice.dto.NoticeCreateRequestDto;
import com.contest.una.notice.dto.NoticeCreateResponseDto;
import com.contest.una.notice.dto.NoticeDetailResponseDto;
import com.contest.una.notice.dto.NoticeListResponse;
import com.contest.una.notice.service.NoticeService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notice")
public class NoticeController {
    private final NoticeService noticeService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NoticeCreateResponseDto> createNotice(
            @AuthenticationPrincipal Long userId,
            @ModelAttribute NoticeCreateRequestDto request) {
        // 이미지가 포함된 multipart/form-data 요청이므로 @ModelAttribute를 사용

        NoticeCreateResponseDto response =
                noticeService.createNotice(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeDetailResponseDto> getNoticeDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable("noticeId") Long noticeId
    ) {
        return ResponseEntity.ok(
                noticeService.getNoticeDetail(userId, noticeId)
        );
    }

    @GetMapping("/{noticeId}/image")
    public ResponseEntity<Resource> getNoticeImage(
            @AuthenticationPrincipal Long userId,
            @PathVariable("noticeId") Long noticeId
    ) {
        Resource image = noticeService.getNoticeImage(userId, noticeId);

        // 파일 확장자(png, jpg)로 Content-Type 결정
        MediaType mediaType = MediaTypeFactory.getMediaType(image)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.empty().cachePrivate())
                .body(image);
    }

    @DeleteMapping("/{noticeId}")
    public ResponseEntity<Void> deleteNotice(
            @AuthenticationPrincipal Long userId,
            @PathVariable("noticeId") Long noticeId
    ) {

        noticeService.deleteNotice(userId, noticeId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public NoticeListResponse getNotices(
            @AuthenticationPrincipal Long userId,
            @RequestParam(name = "page", defaultValue = "0")
            @Min(value = 0, message = "page는 0 이상이어야 합니다.")
            int page,
            @RequestParam(name = "size", defaultValue = "20")
            @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.")
            int size
    ) {
        return noticeService.getNotices(page, size, userId);
    }
}