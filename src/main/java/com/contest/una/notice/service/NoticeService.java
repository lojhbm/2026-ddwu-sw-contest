package com.contest.una.notice.service;

import com.contest.una.event.repository.EventRepository;
import com.contest.una.global.exception.ImageUnreadableException;
import com.contest.una.global.exception.InvalidRequestException;
import com.contest.una.global.exception.NoticeNotFoundException;
import com.contest.una.global.exception.TimetableNotFoundException;
import com.contest.una.notice.ai.NoticeAiAnalyzer;
import com.contest.una.notice.ai.NoticeAnalysis;
import com.contest.una.notice.domain.EventCandidate;
import com.contest.una.notice.domain.NoticeEntity;
import com.contest.una.notice.dto.*;
import com.contest.una.notice.repository.NoticeRepository;
import com.contest.una.notice.storage.NoticeImageStorage;
import com.contest.una.timetable.domain.TimetableEntity;
import com.contest.una.timetable.repository.TimetableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final NoticeImageStorage noticeImageStorage;
    private final EventRepository eventRepository;
    private final NoticeAiAnalyzer noticeAiAnalyzer;
    private final TimetableRepository timetableRepository;

    private static final DateTimeFormatter EVENT_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // AI 호출(최대 수십 초) 동안 DB 커넥션을 잡고 있지 않도록 @Transactional을 붙이지 않음
    // (DB 저장은 noticeRepository.save가 자체 트랜잭션으로 처리)
    public NoticeCreateResponseDto createNotice(Long userId, NoticeCreateRequestDto request) {
        // 선택 입력된 시간표 항목 ID의 양수 여부 검증
        Long userLectureId = request.getUserLectureId();
        if (userLectureId != null && userLectureId <= 0) {
            throw new InvalidRequestException(
                    "userLectureId",
                    "시간표 항목 ID는 양의 정수여야 합니다."
            );
        }

        // image 필수 여부 및 빈 파일 검증
        MultipartFile image = request.getImage();
        if (image == null || image.isEmpty()) {
            throw new InvalidRequestException(
                    "image",
                    "공지 이미지는 필수입니다."
            );
        }

        // 시간표 항목이 지정됐다면 본인 소유인지 확인 (없거나 타인의 항목이면 404)
        // 이미지 저장과 AI 호출 전에 확인해서 불필요한 파일 저장·AI 비용을 막음
        if (userLectureId != null
                && !timetableRepository.existsByIdAndUserId(userLectureId, userId)) {
            throw new TimetableNotFoundException();
        }

        // 용량·형식 검증(413/415/422)을 통과한 이미지만 AI에 보내기 위해 먼저 저장
        String imageFilename = noticeImageStorage.save(image);

        NoticeAnalysis analysis;
        NoticeEntity savedNotice;

        try {
            // AI 분석 (실패 시 422 / 502 / 504)
            analysis = noticeAiAnalyzer.analyze(
                    readBytes(image),
                    mimeTypeOf(imageFilename)
            );

            NoticeEntity notice = NoticeEntity.builder()
                    .userId(userId)
                    .title(analysis.title())
                    .userLectureId(userLectureId)
                    .summary(analysis.summary())
                    .imagePath(imageFilename)
                    .actionItems(analysis.actionItems())
                    .eventCandidates(analysis.eventCandidates())
                    .build();

            savedNotice = noticeRepository.save(notice);

        } catch (RuntimeException e) {
            // AI 분석이나 DB 저장이 실패하면 방금 저장한 이미지 파일이 남지 않도록 삭제
            noticeImageStorage.delete(imageFilename);
            throw e;
        }

        return NoticeCreateResponseDto.builder()
                .noticeId(savedNotice.getId())
                .userLectureId(savedNotice.getUserLectureId())
                .title(savedNotice.getTitle())
                .imageUrl("/api/v1/notice/" + savedNotice.getId() + "/image")
                .summary(savedNotice.getSummary())
                .actionItems(savedNotice.getActionItems())
                .eventCandidates(savedNotice.getEventCandidates().stream()
                        .map(NoticeService::toEventCandidateResponse)
                        .toList())
                .warnings(analysis.warnings())
                .createdAt(savedNotice.getCreatedAt())
                .updatedAt(savedNotice.getUpdatedAt())
                .build();
    }

    private static byte[] readBytes(MultipartFile image) {
        try {
            return image.getBytes();
        } catch (IOException e) {
            throw new ImageUnreadableException(e);
        }
    }

    // NoticeImageStorage가 PNG/JPEG만 저장하므로 확장자로 MIME 타입 결정
    private static String mimeTypeOf(String filename) {
        return filename.toLowerCase(Locale.ROOT).endsWith(".png")
                ? "image/png"
                : "image/jpeg";
    }

    private static EventCandidateResponseDto toEventCandidateResponse(EventCandidate candidate) {
        return EventCandidateResponseDto.builder()
                .title(candidate.getTitle())
                .eventType(candidate.getEventType())
                .eventDate(candidate.getEventDate() == null
                        ? null
                        : candidate.getEventDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .eventTime(candidate.getEventTime() == null
                        ? null
                        : candidate.getEventTime().format(EVENT_TIME_FORMAT))
                .memo(candidate.getMemo())
                .build();
    }

    @Transactional(readOnly = true)
    public NoticeDetailResponseDto getNoticeDetail(Long userId, Long noticeId) {
        if (noticeId == null || noticeId <= 0) {
            throw new InvalidRequestException(
                    "noticeId",
                    "양의 정수 ID를 입력해 주세요."
            );
        }

        NoticeEntity notice = noticeRepository
                .findByIdAndUserId(noticeId, userId)
                .orElseThrow(NoticeNotFoundException::new);

        List<String> actionItems = notice.getActionItems() == null
                ? new ArrayList<>()
                : new ArrayList<>(notice.getActionItems());

        List<EventCandidateResponseDto> eventCandidates =
                notice.getEventCandidates() == null
                        ? new ArrayList<>()
                        : notice.getEventCandidates().stream()
                          .map(NoticeService::toEventCandidateResponse)
                          .toList();

        // 시간표 항목은 선택값이라 userLectureId가 null일 수 있음 (findById(null)은 예외가 남)
        // 연결된 시간표 항목이 이후에 삭제됐어도 공지 조회는 되도록 강의명만 null로 응답
        String lectureName = null;
        if (notice.getUserLectureId() != null) {
            lectureName = timetableRepository.findById(notice.getUserLectureId())
                    .map(timetable -> timetable.getLecture().getName())
                    .orElse(null);
        }

        return NoticeDetailResponseDto.builder()
                .noticeId(notice.getId())
                .userLectureId(notice.getUserLectureId())
                .lectureName(lectureName)
                .title(notice.getTitle())
                .imageUrl("/api/v1/notice/" + notice.getId() + "/image")
                .summary(notice.getSummary())
                .actionItems(actionItems)
                .eventCandidates(eventCandidates)
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }

    @Transactional
    public void deleteNotice(Long userId, Long noticeId) {
        if (noticeId == null || noticeId <= 0) {
            throw new InvalidRequestException(
                    "noticeId",
                    "양의 정수 ID를 입력해 주세요."
            );
        }

        NoticeEntity notice = noticeRepository.findById(noticeId)
                .orElseThrow(NoticeNotFoundException::new);

        // 타인의 공지도 존재 여부를 드러내지 않도록 404로 응답
        if (!userId.equals(notice.getUserId())) {
            throw new NoticeNotFoundException();
        }

        // 이 공지를 출처로 한 일정은 유지하고 sourceNoticeId만 null로 변경
        eventRepository.clearSourceNotice(noticeId);

        noticeRepository.delete(notice);

        // DB 삭제가 커밋된 뒤에 이미지 파일 삭제 (롤백됐는데 파일만 지워지는 것을 방지)
        deleteImageAfterCommit(notice.getImagePath());
    }

    private void deleteImageAfterCommit(String filename) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            noticeImageStorage.delete(filename);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        noticeImageStorage.delete(filename);
                    }
                }
        );
    }

    @Transactional(readOnly = true)
    public Resource getNoticeImage(Long userId, Long noticeId) {
        if (noticeId == null || noticeId <= 0) {
            throw new InvalidRequestException(
                    "noticeId",
                    "양의 정수 ID를 입력해 주세요."
            );
        }

        // 본인 소유의 공지만 조회. 없거나 타인의 공지면 404
        NoticeEntity notice = noticeRepository
                .findByIdAndUserId(noticeId, userId)
                .orElseThrow(NoticeNotFoundException::new);

        return noticeImageStorage.load(notice.getImagePath());
    }

    // 시간표 항목 ID -> 강의명 (본인 소유 항목만 조회)
    private Map<Long, String> findLectureNames(List<NoticeEntity> notices, Long userId) {
        List<Long> userLectureIds = notices.stream()
                .map(NoticeEntity::getUserLectureId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, String> lectureNames = new HashMap<>();
        if (userLectureIds.isEmpty()) {
            return lectureNames;
        }

        for (TimetableEntity timetable
                : timetableRepository.findAllWithLectureByIdInAndUserId(userLectureIds, userId)) {
            lectureNames.put(timetable.getId(), timetable.getLecture().getName());
        }
        return lectureNames;
    }

    @Transactional(readOnly = true)
    public NoticeListResponse getNotices(int page, int size, Long userId) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Page<NoticeEntity> noticePage =
                noticeRepository.findByUserId(userId, pageable);

        // 이번 페이지 공지들의 강의명을 한 번의 쿼리로 조회 (공지마다 조회하는 N+1 방지)
        Map<Long, String> lectureNames = findLectureNames(noticePage.getContent(), userId);

        List<NoticeListItemResponse> items = noticePage.getContent()
                .stream()
                .map(notice -> new NoticeListItemResponse(
                        notice.getId(),
                        notice.getUserLectureId(),
                        // 연결된 시간표 항목이 없거나 삭제됐으면 null
                        notice.getUserLectureId() == null
                                ? null
                                : lectureNames.get(notice.getUserLectureId()),
                        notice.getTitle(),
                        notice.getSummary(),
                        notice.getCreatedAt()
                ))
                .toList();

        return new NoticeListResponse(
                items,
                noticePage.getNumber(),
                noticePage.getSize(),
                noticePage.getTotalElements(),
                noticePage.getTotalPages()
        );
    }

}