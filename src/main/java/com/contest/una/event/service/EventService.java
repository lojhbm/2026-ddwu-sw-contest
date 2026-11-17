package com.contest.una.event.service;

import com.contest.una.event.domain.EventEntity;
import com.contest.una.event.dto.EventCreateRequestDto;
import com.contest.una.event.dto.EventResponse;
import com.contest.una.event.dto.EventListResponseDto;
import com.contest.una.event.dto.EventResponse;
import com.contest.una.event.repository.EventRepository;
import com.contest.una.global.exception.EventNotFoundException;
import com.contest.una.global.exception.InvalidRequestException;
import com.contest.una.global.exception.NoticeNotFoundException;
import com.contest.una.global.exception.TimetableNotFoundException;
import com.contest.una.notice.domain.NoticeEntity;
import com.contest.una.notice.repository.NoticeRepository;
import com.contest.una.timetable.repository.TimetableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EventService {
    private static final Set<String> EVENT_TYPES = Set.of("assignment", "exam", "other");
    private static final int TITLE_MAX_LENGTH = 300;
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final Pattern TIME_PATTERN = Pattern.compile("\\d{2}:\\d{2}:\\d{2}");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final EventRepository eventRepository;
    private final NoticeRepository noticeRepository;
    private final TimetableRepository timetableRepository;

    @Transactional
    public EventResponse createEvent(Long userId, EventCreateRequestDto request) {
        // 1. 요청 값 형식 검증 (실패하면 400 VALIDATION_ERROR)
        validatePositiveId("sourceNoticeId", request.getSourceNoticeId());
        validatePositiveId("userLectureId", request.getUserLectureId());
        String title = validateTitle(request.getTitle());
        String eventType = validateEventType(request.getEventType());
        LocalDate eventDate = parseEventDate(request.getEventDate());
        LocalTime eventTime = parseEventTime(request.getEventTime());

        // 2. 출처 공지가 있으면 본인 소유인지 확인 (없거나 타인의 공지면 404)
        NoticeEntity notice = null;
        if (request.getSourceNoticeId() != null) {
            notice = noticeRepository
                    .findByIdAndUserId(request.getSourceNoticeId(), userId)
                    .orElseThrow(NoticeNotFoundException::new);
        }

        // 3. 시간표 항목: 요청값이 있으면 본인 소유인지 확인, 없으면 출처 공지의 값을 기본값으로 사용
        Long userLectureId = request.getUserLectureId();
        if (userLectureId != null) {
            if (!timetableRepository.existsByIdAndUserId(userLectureId, userId)) {
                throw new TimetableNotFoundException();
            }
        } else if (notice != null) {
            userLectureId = notice.getUserLectureId();
        }

        EventEntity event = EventEntity.builder()
                .userId(userId)
                .sourceNoticeId(request.getSourceNoticeId())
                .userLectureId(userLectureId)
                .title(title)
                .eventType(eventType)
                .eventDate(eventDate)
                .eventTime(eventTime)
                .memo(request.getMemo())
                .build();

        EventEntity saved = eventRepository.save(event);

        return EventResponse.builder()
                .eventId(saved.getId())
                .sourceNoticeId(saved.getSourceNoticeId())
                .userLectureId(saved.getUserLectureId())
                .title(saved.getTitle())
                .eventType(saved.getEventType())
                .eventDate(saved.getEventDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .eventTime(saved.getEventTime() == null
                        ? null
                        : saved.getEventTime().format(TIME_FORMAT))
                .memo(saved.getMemo())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    // 캘린더 일정 목록 조회: 기간(startDate~endDate 포함) 안의 본인 일정. 강의·유형은 선택 필터
    @Transactional(readOnly = true)
    public EventListResponseDto getEvents(
            Long userId,
            String startDateText,
            String endDateText,
            Long userLectureId,
            String eventTypeText
    ) {
        // 1. 요청 값 형식 검증 (실패하면 400 VALIDATION_ERROR)
        LocalDate startDate = parseDate("startDate", startDateText);
        LocalDate endDate = parseDate("endDate", endDateText);
        if (endDate.isBefore(startDate)) {
            throw new InvalidRequestException("endDate", "종료일은 시작일보다 빠를 수 없습니다.");
        }
        validatePositiveId("userLectureId", userLectureId);
        String eventType = eventTypeText == null ? null : validateEventType(eventTypeText);

        // 2. 강의로 거르는 경우 본인 시간표 항목인지 확인 (없거나 타인의 항목이면 404)
        if (userLectureId != null
                && !timetableRepository.existsByIdAndUserId(userLectureId, userId)) {
            throw new TimetableNotFoundException();
        }

        List<EventResponse> items = eventRepository
                .findAllInRange(userId, startDate, endDate, userLectureId, eventType)
                .stream()
                .map(EventService::toResponse)
                .toList();

        return EventListResponseDto.builder()
                .items(items)
                .build();
    }

    // 일정 단건 조회 (없거나 타인의 일정이면 404)
    @Transactional(readOnly = true)
    public EventResponse getEvent(Long userId, Long eventId) {
        validateEventId(eventId);

        EventEntity event = eventRepository
                .findByIdAndUserId(eventId, userId)
                .orElseThrow(EventNotFoundException::new);

        return toResponse(event);
    }

    // 일정 삭제 (없거나 타인의 일정이면 404)
    @Transactional
    public void deleteEvent(Long userId, Long eventId) {
        validateEventId(eventId);

        EventEntity event = eventRepository
                .findByIdAndUserId(eventId, userId)
                .orElseThrow(EventNotFoundException::new);

        eventRepository.delete(event);
    }

    private static void validateEventId(Long eventId) {
        if (eventId == null || eventId <= 0) {
            throw new InvalidRequestException("eventId", "양의 정수 ID를 입력해 주세요.");
        }
    }

    private static EventResponse toResponse(EventEntity event) {
        return EventResponse.builder()
                .eventId(event.getId())
                .sourceNoticeId(event.getSourceNoticeId())
                .userLectureId(event.getUserLectureId())
                .title(event.getTitle())
                .eventType(event.getEventType())
                .eventDate(event.getEventDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .eventTime(event.getEventTime() == null
                        ? null
                        : event.getEventTime().format(TIME_FORMAT))
                .memo(event.getMemo())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }

    // 선택 입력된 ID의 양수 여부 검증
    private static void validatePositiveId(String field, Long id) {
        if (id != null && id <= 0) {
            throw new InvalidRequestException(field, "양의 정수 ID를 입력해 주세요.");
        }
    }

    private static String validateTitle(String title) {
        if (title == null) {
            throw new InvalidRequestException("title", "필수 값입니다.");
        }
        if (title.isBlank()) {
            throw new InvalidRequestException("title", "공백만 입력할 수 없습니다.");
        }
        // 이모지 등도 한 글자로 세도록 code point 기준으로 길이 확인
        if (title.codePointCount(0, title.length()) > TITLE_MAX_LENGTH) {
            throw new InvalidRequestException("title", "최대 300자까지 입력할 수 있습니다.");
        }
        return title;
    }

    private static String validateEventType(String eventType) {
        if (eventType == null) {
            throw new InvalidRequestException("eventType", "필수 값입니다.");
        }
        if (!EVENT_TYPES.contains(eventType)) {
            throw new InvalidRequestException(
                    "eventType",
                    "assignment, exam, other 중 하나여야 합니다."
            );
        }
        return eventType;
    }

    private static LocalDate parseEventDate(String eventDate) {
        return parseDate("eventDate", eventDate);
    }

    // 필수 날짜 값 검증. field는 에러 응답의 errors[].field에 들어감 (eventDate, startDate, endDate 등)
    private static LocalDate parseDate(String field, String value) {
        if (value == null) {
            throw new InvalidRequestException(field, "필수 값입니다.");
        }
        // 2026-1-5 같은 형식을 막고, 2026-02-30 같은 존재하지 않는 날짜는 parse에서 걸러짐
        if (!DATE_PATTERN.matcher(value).matches()) {
            throw new InvalidRequestException(field, "YYYY-MM-DD 형식으로 입력해 주세요.");
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeException e) {
            throw new InvalidRequestException(field, "존재하는 날짜를 입력해 주세요.");
        }
    }

    // 시간은 선택값: null이면 시간 미지정
    private static LocalTime parseEventTime(String eventTime) {
        if (eventTime == null) {
            return null;
        }
        if (!TIME_PATTERN.matcher(eventTime).matches()) {
            throw new InvalidRequestException("eventTime", "HH:mm:ss 형식으로 입력해 주세요.");
        }
        try {
            return LocalTime.parse(eventTime);
        } catch (DateTimeException e) {
            throw new InvalidRequestException("eventTime", "00:00:00부터 23:59:59 사이로 입력해 주세요.");
        }
    }
}