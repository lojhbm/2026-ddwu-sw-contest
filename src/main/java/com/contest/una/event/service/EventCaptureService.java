package com.contest.una.event.service;

import com.contest.una.event.ai.AssignmentAiAnalyzer;
import com.contest.una.event.ai.AssignmentAnalysis;
import com.contest.una.event.ai.AssignmentCandidate;
import com.contest.una.event.domain.CaptureImageEntity;
import com.contest.una.event.dto.EventCaptureRequestDto;
import com.contest.una.event.dto.EventCaptureResponseDto;
import com.contest.una.event.repository.CaptureImageRepository;
import com.contest.una.event.repository.EventRepository;
import com.contest.una.global.exception.ImageNotFoundException;
import com.contest.una.global.exception.ImageUnreadableException;
import com.contest.una.global.exception.InvalidRequestException;
import com.contest.una.notice.storage.NoticeImageStorage;
import com.contest.una.timetable.domain.TimetableEntity;
import com.contest.una.timetable.repository.TimetableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventCaptureService {
    private static final Duration UNUSED_IMAGE_TTL = Duration.ofHours(24);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final AssignmentAiAnalyzer assignmentAiAnalyzer;
    private final NoticeImageStorage imageStorage;
    private final CaptureImageRepository captureImageRepository;
    private final EventRepository eventRepository;
    private final TimetableRepository timetableRepository;

    // AI 호출 동안 DB 커넥션을 잡지 않도록 @Transactional을 붙이지 않음
    public EventCaptureResponseDto capture(Long userId, EventCaptureRequestDto request) {
        MultipartFile image = request.getImage();
        if (image == null || image.isEmpty()) {
            throw new InvalidRequestException("image", "과제 캡처 이미지는 필수입니다.");
        }

        String filename = imageStorage.save(image);

        AssignmentAnalysis analysis;
        CaptureImageEntity saved;
        try {
            analysis = assignmentAiAnalyzer.analyze(readBytes(image), mimeTypeOf(filename));
            saved = captureImageRepository.save(CaptureImageEntity.builder()
                    .userId(userId)
                    .imagePath(filename)
                    .build());
        } catch (RuntimeException e) {
            imageStorage.delete(filename);
            throw e;
        }

        List<TimetableEntity> myLectures = timetableRepository.findAllWithLectureByUserId(userId);
        List<EventCaptureResponseDto.Candidate> candidates = analysis.candidates().stream()
                .map(c -> toCandidate(userId, c, myLectures))
                .toList();

        return EventCaptureResponseDto.builder()
                .sourceImageId(saved.getId())
                .imageUrl("/api/v1/event/capture/" + saved.getId() + "/image")
                .candidates(candidates)
                .warnings(analysis.warnings())
                .build();
    }

    @Transactional(readOnly = true)
    public Resource getCaptureImage(Long userId, Long imageId) {
        if (imageId == null || imageId <= 0) {
            throw new InvalidRequestException("imageId", "양의 정수 ID를 입력해 주세요.");
        }
        CaptureImageEntity captureImage = captureImageRepository
                .findByIdAndUserId(imageId, userId)
                .orElseThrow(ImageNotFoundException::new);
        return imageStorage.load(captureImage.getImagePath());
    }

    // 일정 삭제 후 호출. 같은 이미지를 쓰는 일정이 없으면 이미지도 삭제
    @Transactional
    public void deleteIfUnused(Long imageId) {
        if (imageId == null || eventRepository.existsBySourceImageId(imageId)) {
            return;
        }
        captureImageRepository.findById(imageId).ifPresent(this::delete);
    }

    @Transactional
    public int deleteUnusedImages() {
        List<CaptureImageEntity> unused =
                captureImageRepository.findUnusedCreatedBefore(Instant.now().minus(UNUSED_IMAGE_TTL));
        unused.forEach(this::delete);
        return unused.size();
    }

    private void delete(CaptureImageEntity captureImage) {
        captureImageRepository.delete(captureImage);
        String filename = captureImage.getImagePath();

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            imageStorage.delete(filename);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                imageStorage.delete(filename);
            }
        });
    }

    private EventCaptureResponseDto.Candidate toCandidate(
            Long userId,
            AssignmentCandidate candidate,
            List<TimetableEntity> myLectures
    ) {
        TimetableEntity matched = matchLecture(candidate.courseName(), myLectures);
        Long userLectureId = matched == null ? null : matched.getId();

        boolean duplicate = candidate.eventDate() != null
                && eventRepository.countSame(userId, candidate.title(), candidate.eventDate(),
                        candidate.eventTime(), userLectureId) > 0;

        return EventCaptureResponseDto.Candidate.builder()
                .courseName(candidate.courseName())
                .userLectureId(userLectureId)
                .lectureName(matched == null ? null : matched.getLecture().getName())
                .title(candidate.title())
                .eventType(candidate.eventType())
                .eventDate(candidate.eventDate() == null
                        ? null
                        : candidate.eventDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .eventTime(candidate.eventTime() == null
                        ? null
                        : candidate.eventTime().format(TIME_FORMAT))
                .memo(candidate.memo())
                .needsCheck(!candidate.checkReasons().isEmpty())
                .checkReasons(candidate.checkReasons())
                .duplicate(duplicate)
                .build();
    }

    // 공백·대소문자 무시. 완전 일치 우선, 없으면 부분 일치가 하나뿐일 때만 연결
    private static TimetableEntity matchLecture(String courseName, List<TimetableEntity> myLectures) {
        String target = normalize(courseName);
        if (target == null) {
            return null;
        }

        for (TimetableEntity timetable : myLectures) {
            if (target.equals(normalize(timetable.getLecture().getName()))) {
                return timetable;
            }
        }

        List<TimetableEntity> partial = myLectures.stream()
                .filter(t -> {
                    String name = normalize(t.getLecture().getName());
                    return name != null && (name.contains(target) || target.contains(name));
                })
                .toList();
        return partial.size() == 1 ? partial.get(0) : null;
    }

    private static String normalize(String text) {
        if (text == null) {
            return null;
        }
        String normalized = text.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private static byte[] readBytes(MultipartFile image) {
        try {
            return image.getBytes();
        } catch (IOException e) {
            throw new ImageUnreadableException(e);
        }
    }

    private static String mimeTypeOf(String filename) {
        return filename.toLowerCase(Locale.ROOT).endsWith(".png") ? "image/png" : "image/jpeg";
    }
}
