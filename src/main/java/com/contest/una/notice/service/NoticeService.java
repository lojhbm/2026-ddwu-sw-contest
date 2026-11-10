package com.contest.una.notice.service;

import com.contest.una.global.exception.InvalidRequestException;
import com.contest.una.notice.domain.NoticeEntity;
import com.contest.una.notice.dto.*;
import com.contest.una.notice.repository.NoticeRepository;
import com.contest.una.notice.storage.NoticeImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final NoticeImageStorage noticeImageStorage;

    @Transactional
    public NoticeCreateResponseDto createNotice(Long userId, NoticeCreateRequestDto request) {
        // 선택 입력된 시간표 항목 ID의 양수 여부 검증
        Long userLectureId = request.getUserLectureId();
        if (userLectureId != null && userLectureId <= 0) {
            throw new InvalidRequestException(
                    "userLectureId",
                    "시간표 항목 ID는 양의 정수여야 합니다."
            );
        }

        // Todo: 시간표 연동 구현 후 검증한 ID로 연결
        Long linkedUserLectureId = null;

        // image 필수 여부 및 빈 파일 검증
        MultipartFile image = request.getImage();
        if (image == null || image.isEmpty()) {
            throw new InvalidRequestException(
                    "image",
                    "공지 이미지는 필수입니다."
            );
        }
        String imageFilename = noticeImageStorage.save(image);

        // Todo: title, summary, actionItems, eventCandidates, warnings는 추후 ai 기능 연동 시 ai가 요약해준 내용을 저장하도록 합니다.
        String title = "title 임시 문자열";
        String summary = "summary 임시 문자열";
        List<String> actionItems = new ArrayList<>();
        List<EventCandidateResponseDto> eventCandidates = new ArrayList<>();
        List<String> warnings = new ArrayList<>(); // 아직은 빈 배열로 구현, 추후 ai 기능 구현시 보완

        NoticeEntity notice = NoticeEntity.builder()
                .userId(userId)
                .title(title)
                .userLectureId(linkedUserLectureId)
                .summary(summary)
                .imagePath(imageFilename)
                .actionItems(actionItems)
                .build();

        NoticeEntity savedNotice = noticeRepository.save(notice);

        NoticeCreateResponseDto response = NoticeCreateResponseDto.builder()
                .noticeId(savedNotice.getId())
                .userLectureId(savedNotice.getUserLectureId())
                .title(savedNotice.getTitle())
                .imageUrl("/api/v1/notice/" + savedNotice.getId() + "/image")
                .summary(savedNotice.getSummary())
                .actionItems(savedNotice.getActionItems())
                .eventCandidates(eventCandidates)
                .warnings(warnings)
                .createdAt(savedNotice.getCreatedAt())
                .updatedAt(savedNotice.getUpdatedAt())
                .build();

        return response;
        // TODO: DB 트랜잭션 롤백 시 저장된 이미지 파일 삭제
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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "NOTICE_NOT_FOUND"
                ));

        List<String> actionItems = notice.getActionItems() == null
                ? new ArrayList<>()
                : new ArrayList<>(notice.getActionItems());

        List<EventCandidateResponseDto> eventCandidates =
                notice.getEventCandidates() == null
                        ? new ArrayList<>()
                        : notice.getEventCandidates().stream()
                          .map(candidate -> EventCandidateResponseDto.builder()
                                            .title(candidate.getTitle())
                                            .eventType(candidate.getEventType())
                                            .eventDate(candidate.getEventDate() == null
                                                       ? null
                                                       : candidate.getEventDate().format(
                                                    DateTimeFormatter.ISO_LOCAL_DATE))
                                            .eventTime(candidate.getEventTime() == null
                                                       ? null
                                                       : candidate.getEventTime().format(
                                                    DateTimeFormatter.ofPattern("HH:mm:ss")))
                                            .memo(candidate.getMemo())
                                            .build())
                          .toList();

        return NoticeDetailResponseDto.builder()
                .noticeId(notice.getId())
                .userLectureId(notice.getUserLectureId())
                // TODO: 시간표 연동 후 실제 강의명 반환
                .lectureName(null)
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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "NOTICE_NOT_FOUND"
                ));

        if (!userId.equals(notice.getUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "NOTICE_NOT_FOUND"
            );
        }

        noticeRepository.delete(notice);
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

        List<NoticeListItemResponse> items = noticePage.getContent()
                .stream()
                .map(notice -> new NoticeListItemResponse(
                        notice.getId(),
                        null, // userLectureId: 시간표 구현 후 연결
                        null, // lectureName: 시간표 구현 후 연결
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
