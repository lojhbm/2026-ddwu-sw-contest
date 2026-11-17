package com.contest.una.timetable.service;

import com.contest.una.timetable.domain.LectureEntity;
import com.contest.una.timetable.domain.TimetableEntity;
import com.contest.una.timetable.dto.ClassroomResponse;
import com.contest.una.timetable.dto.LectureSearchResponse;
import com.contest.una.timetable.repository.LectureRepository;
import com.contest.una.timetable.repository.TimetableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;
    private final TimetableRepository timetableRepository;

    @Transactional(readOnly = true)
    public LectureSearchResponse search(
            Long userId,
            Integer academicYear,
            String semester,
            String keyword,
            int page,
            int size
    ) {
        String trimmedKeyword = keyword == null || keyword.isBlank() ? null : keyword.strip();

        Page<LectureRepository.SectionKey> sections = lectureRepository.searchSections(
                academicYear, semester, toLikePattern(trimmedKeyword), PageRequest.of(page, size));

        List<String> keys = sections.getContent().stream()
                .map(s -> sectionKey(s.getCourseCode(), s.getSectionCode()))
                .toList();

        Map<String, List<LectureEntity>> lecturesByKey = keys.isEmpty()
                ? Map.of()
                : lectureRepository.findAllWithClassroomBySections(academicYear, semester, keys)
                  .stream()
                  .collect(Collectors.groupingBy(LectureService::sectionKey));

        Set<String> myKeys = timetableRepository.findAllByUserIdAndTerm(userId, academicYear, semester)
                .stream()
                .map(TimetableEntity::getLecture)
                .map(LectureService::sectionKey)
                .collect(Collectors.toSet());

        List<LectureSearchResponse.Item> items = keys.stream()
                .filter(lecturesByKey::containsKey)
                .map(key -> toItem(lecturesByKey.get(key), myKeys.contains(key)))
                .toList();

        return new LectureSearchResponse(
                academicYear,
                semester,
                trimmedKeyword,
                items,
                sections.getNumber(),
                sections.getSize(),
                sections.getTotalElements(),
                sections.getTotalPages()
        );
    }

    private static LectureSearchResponse.Item toItem(List<LectureEntity> lectures, boolean inMyTimetable) {
        LectureEntity first = lectures.get(0);
        return new LectureSearchResponse.Item(
                first.getCourseCode(),
                first.getSectionCode(),
                first.getName(),
                lectures.stream().map(LectureEntity::getId).toList(),
                lectures.stream()
                        .map(l -> new LectureSearchResponse.Schedule(
                                l.getId(),
                                l.getTimeWeek(),
                                l.getTimeStartsAt(),
                                l.getTimeEndsAt(),
                                l.getClassroom() == null ? null : ClassroomResponse.from(l.getClassroom())))
                        .toList(),
                inMyTimetable
        );
    }

    // 공백 제거·소문자 변환 후 부분 일치. LIKE 특수문자(%, _, \)는 이스케이프
    private static String toLikePattern(String keyword) {
        if (keyword == null) {
            return null;
        }
        String normalized = keyword.replaceAll("\\s+", "")
                .toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + normalized + "%";
    }

    private static String sectionKey(LectureEntity lecture) {
        return sectionKey(lecture.getCourseCode(), lecture.getSectionCode());
    }

    private static String sectionKey(String courseCode, String sectionCode) {
        return courseCode + "|" + sectionCode;
    }
}
