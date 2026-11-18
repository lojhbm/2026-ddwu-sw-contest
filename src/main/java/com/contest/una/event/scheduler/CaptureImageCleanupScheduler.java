package com.contest.una.event.scheduler;

import com.contest.una.event.service.EventCaptureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaptureImageCleanupScheduler {
    private final EventCaptureService eventCaptureService;

    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void deleteUnusedImages() {
        int deleted = eventCaptureService.deleteUnusedImages();
        if (deleted > 0) {
            log.info("일정으로 저장되지 않은 과제 캡처 이미지 {}건을 삭제했습니다.", deleted);
        }
    }
}
