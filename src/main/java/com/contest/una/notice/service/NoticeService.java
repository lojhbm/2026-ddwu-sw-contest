package com.contest.una.notice.service;

import com.contest.una.global.exception.InvalidRequestException;
import com.contest.una.notice.domain.NoticeEntity;
import com.contest.una.notice.dto.EventCandidateResponseDto;
import com.contest.una.notice.dto.NoticeCreateRequestDto;
import com.contest.una.notice.dto.NoticeCreateResponseDto;
import com.contest.una.notice.repository.NoticeRepository;
import com.contest.una.notice.storage.NoticeImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
}
