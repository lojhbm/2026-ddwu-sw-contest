package com.contest.una.classroom.service;

import com.contest.una.classroom.domain.ClassroomEntity;
import com.contest.una.classroom.dto.ClassroomAvailabilityRequest;
import com.contest.una.classroom.dto.ClassroomAvailabilityResponse;
import com.contest.una.classroom.repository.ClassroomRepository;
import com.contest.una.global.exception.BuildingNotFoundException;
import com.contest.una.global.exception.FreeSlotOverlapsLectureException;
import com.contest.una.global.exception.InvalidRequestException;
import com.contest.una.global.exception.InvalidTimeRangeException;
import com.contest.una.timetable.domain.LectureEntity;
import com.contest.una.timetable.domain.TimetableEntity;
import com.contest.una.timetable.repository.LectureRepository;
import com.contest.una.timetable.repository.TimetableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassroomService {
    private static final Set<String> SEMESTERS = Set.of("1", "summer", "2", "winter");
    private static final Pattern TIME_PATTERN = Pattern.compile("\\d{2}:\\d{2}");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final ClassroomRepository classroomRepository;
    private final LectureRepository lectureRepository;
    private final TimetableRepository timetableRepository;

    @Transactional(readOnly = true)
    public ClassroomAvailabilityResponse getAvailability(
            Long userId,
            ClassroomAvailabilityRequest request
    ) {
        // 1. 요청 값 형식 검증 (실패하면 400 VALIDATION_ERROR)
        Integer academicYear = validateAcademicYear(request.getAcademicYear());
        String semester = validateSemester(request.getSemester());
        Integer timeWeek = validateTimeWeek(request.getTimeWeek());
        LocalTime startTime = parseTime("startTime", request.getStartTime());
        LocalTime endTime = parseTime("endTime", request.getEndTime());
        String previousBuilding = normalizeBuildingName(request.getPreviousBuildingName());
        String nextBuilding = normalizeBuildingName(request.getNextBuildingName());

        // 2. 시작 시각이 종료 시각 이상이면 400 INVALID_TIME_RANGE
        if (!startTime.isBefore(endTime)) {
            throw new InvalidTimeRangeException();
        }

        // 3. 전달한 건물명이 실제 존재하는지 확인 (없으면 404)
        validateBuildingExists(previousBuilding);
        validateBuildingExists(nextBuilding);

        // 4. 내 시간표의 같은 연도·학기·요일 수업과 조회 구간이 겹치면 409
        if (overlapsMyTimetable(userId, academicYear, semester, timeWeek, startTime, endTime)) {
            throw new FreeSlotOverlapsLectureException();
        }

        // 5. 조회 구간에 수업이 없는 강의실과, 강의실별 다음 수업 시작 시각
        List<ClassroomEntity> classrooms = classroomRepository
                .findAvailable(academicYear, semester, timeWeek, startTime, endTime);

        Map<Long, LocalTime> nextStarts = classrooms.isEmpty()
                ? Map.of()
                : lectureRepository
                  .findNextLectureStarts(
                          classrooms.stream().map(ClassroomEntity::getId).toList(),
                          academicYear, semester, timeWeek, endTime)
                  .stream()
                  .collect(Collectors.toMap(
                          LectureRepository.NextLectureStart::getClassroomId,
                          LectureRepository.NextLectureStart::getStartsAt));

        // 6. 건물 정렬: 직후 수업 건물 -> 직전 수업 건물 -> 나머지 건물명 오름차순, 강의실은 번호 오름차순
        Comparator<ClassroomEntity> order = Comparator
                .comparingInt((ClassroomEntity c) ->
                        buildingPriority(c.getBuildingName(), previousBuilding, nextBuilding))
                .thenComparing(ClassroomEntity::getBuildingName)
                .thenComparing(ClassroomEntity::getRoomName);

        // 같은 건물은 하나로 묶음 (정렬된 순서가 LinkedHashMap에 유지됨)
        Map<String, List<ClassroomAvailabilityResponse.ClassroomItem>> byBuilding =
                new LinkedHashMap<>();
        classrooms.stream()
                .sorted(order)
                .forEach(c -> byBuilding
                        .computeIfAbsent(c.getBuildingName(), name -> new ArrayList<>())
                        .add(ClassroomAvailabilityResponse.ClassroomItem.builder()
                                .classroomId(c.getId())
                                .roomName(c.getRoomName())
                                .nextLectureStartTime(formatOrNull(nextStarts.get(c.getId())))
                                .build()));

        List<ClassroomAvailabilityResponse.BuildingItem> buildings = byBuilding.entrySet()
                .stream()
                .map(e -> ClassroomAvailabilityResponse.BuildingItem.builder()
                        .buildingName(e.getKey())
                        .classroomCount(e.getValue().size())
                        .classrooms(e.getValue())
                        .build())
                .toList();

        return ClassroomAvailabilityResponse.builder()
                .academicYear(academicYear)
                .semester(semester)
                .timeWeek(timeWeek)
                .startTime(startTime.format(TIME_FORMAT))
                .endTime(endTime.format(TIME_FORMAT))
                .totalCount(classrooms.size())
                .buildings(buildings)
                .build();
    }

    private boolean overlapsMyTimetable(
            Long userId,
            Integer academicYear,
            String semester,
            Integer timeWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {
        return timetableRepository.findAllByUserIdAndTerm(userId, academicYear, semester)
                .stream()
                .map(TimetableEntity::getLecture)
                .anyMatch(lecture -> overlaps(lecture, timeWeek, startTime, endTime));
    }

    // 맞닿는 시각(수업 종료 == 조회 시작 등)은 겹치지 않는 것으로 봄
    private static boolean overlaps(
            LectureEntity lecture,
            Integer timeWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {
        return lecture.getTimeWeek().equals(timeWeek)
                && lecture.getTimeStartsAt().isBefore(endTime)
                && startTime.isBefore(lecture.getTimeEndsAt());
    }

    private void validateBuildingExists(String buildingName) {
        if (buildingName != null && !classroomRepository.existsByBuildingName(buildingName)) {
            throw new BuildingNotFoundException();
        }
    }

    private static int buildingPriority(String buildingName, String previous, String next) {
        if (buildingName.equals(next)) {
            return 0;
        }
        if (buildingName.equals(previous)) {
            return 1;
        }
        return 2;
    }

    private static String formatOrNull(LocalTime time) {
        return time == null ? null : time.format(TIME_FORMAT);
    }

    private static Integer validateAcademicYear(Integer academicYear) {
        if (academicYear == null) {
            throw new InvalidRequestException("academicYear", "필수 값입니다.");
        }
        if (academicYear < 1000 || academicYear > 9999) {
            throw new InvalidRequestException("academicYear", "4자리 연도를 입력해 주세요.");
        }
        return academicYear;
    }

    private static String validateSemester(String semester) {
        if (semester == null) {
            throw new InvalidRequestException("semester", "필수 값입니다.");
        }
        if (!SEMESTERS.contains(semester)) {
            throw new InvalidRequestException(
                    "semester",
                    "1, summer, 2, winter 중 하나여야 합니다."
            );
        }
        return semester;
    }

    private static Integer validateTimeWeek(Integer timeWeek) {
        if (timeWeek == null) {
            throw new InvalidRequestException("timeWeek", "필수 값입니다.");
        }
        if (timeWeek < 1 || timeWeek > 7) {
            throw new InvalidRequestException("timeWeek", "월요일 1 ~ 일요일 7 사이로 입력해 주세요.");
        }
        return timeWeek;
    }

    // HH:mm 형식만 허용 (9:00 같은 형식은 막고, 24:00 같은 범위 밖 값은 parse에서 걸러짐)
    private static LocalTime parseTime(String field, String value) {
        if (value == null) {
            throw new InvalidRequestException(field, "필수 값입니다.");
        }
        if (!TIME_PATTERN.matcher(value).matches()) {
            throw new InvalidRequestException(field, "HH:mm 형식으로 입력해 주세요.");
        }
        try {
            return LocalTime.parse(value);
        } catch (DateTimeException e) {
            throw new InvalidRequestException(field, "00:00부터 23:59 사이로 입력해 주세요.");
        }
    }

    // 직전/직후 수업이 없거나 위치가 없으면 생략하는 값이라, null과 공백은 "없음"으로 처리
    private static String normalizeBuildingName(String buildingName) {
        if (buildingName == null || buildingName.isBlank()) {
            return null;
        }
        return buildingName.strip();
    }
}