package com.contest.una.timetable.service;

import com.contest.una.global.exception.LectureNotFoundException;
import com.contest.una.global.exception.TimetableAlreadyExistsException;
import com.contest.una.global.exception.TimetableTimeConflictException;
import com.contest.una.timetable.domain.LectureEntity;
import com.contest.una.timetable.domain.TimetableEntity;
import com.contest.una.timetable.dto.AddTimetableRequestDto;
import com.contest.una.timetable.dto.AddTimetableResponseDto;
import com.contest.una.timetable.repository.LectureRepository;
import com.contest.una.timetable.repository.TimetableRepository;
import com.contest.una.user.domain.UserEntity;
import com.contest.una.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableRepository timetableRepository;
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;

    @Transactional
    public AddTimetableResponseDto addTimetable(
            Long userId,
            AddTimetableRequestDto request
    ) {
        List<Long> lectureIds = request.lectureIds();

        // 요청한 연도·학기의 강의 조회 -> 하나라도 없으면 404
        List<LectureEntity> lectures =
                lectureRepository.findAllWithClassroomByIdInAndTerm(
                        lectureIds,
                        request.academicYear(),
                        request.semester()
                );
        if (lectures.size() != lectureIds.size()) {
            throw new LectureNotFoundException();
        }

        // 내 시간표에 이미 등록된 강의가 포함되어 있으면 409
        if (timetableRepository.existsByUserIdAndLectureIdIn(
                userId, lectureIds)) {
            throw new TimetableAlreadyExistsException();
        }

        // 요청한 lectureIds 순서대로 강의 정렬
        Map<Long, LectureEntity> lectureById = lectures.stream()
                .collect(Collectors.toMap(
                        LectureEntity::getId,
                        Function.identity()
                ));
        List<LectureEntity> requested = lectureIds.stream()
                .map(lectureById::get)
                .toList();

        // 시간 겹침 검사 (같은 학기 기존 시간표 + 이번 요청 안의 강의끼리) -> 409
        List<LectureEntity> existing = timetableRepository
                .findAllByUserIdAndTerm(
                        userId,
                        request.academicYear(),
                        request.semester()
                )
                .stream()
                .map(TimetableEntity::getLecture)
                .toList();
        validateNoTimeConflict(existing, requested);

        // 시간표 항목 생성 및 저장
        UserEntity user = userRepository.getReferenceById(userId);
        List<TimetableEntity> timetables = requested.stream()
                .map(lecture -> TimetableEntity.builder()
                        .user(user)
                        .lecture(lecture)
                        .build())
                .toList();

        List<TimetableEntity> saved;
        try {
            saved = timetableRepository.saveAll(timetables);
        } catch (DataIntegrityViolationException e) {
            throw new TimetableAlreadyExistsException();
        }

        // 응답 변환
        return AddTimetableResponseDto.of(
                request.academicYear(),
                request.semester(),
                saved
        );
    }

    private void validateNoTimeConflict(
            List<LectureEntity> existing,
            List<LectureEntity> requested
    ) {
        List<LectureEntity> occupied = new ArrayList<>(existing);
        for (LectureEntity lecture : requested) {
            if (occupied.stream().anyMatch(lecture::overlapsWith)) {
                throw new TimetableTimeConflictException();
            }
            occupied.add(lecture);
        }
    }
}