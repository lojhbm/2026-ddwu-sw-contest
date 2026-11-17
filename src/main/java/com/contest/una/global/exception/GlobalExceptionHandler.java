package com.contest.una.global.exception;

import com.contest.una.global.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(
            InvalidRequestException e
    ) {
        ErrorResponse response = new ErrorResponse(
                "VALIDATION_ERROR",
                "요청 값을 확인해 주세요.",
                List.of(
                        new ErrorResponse.FieldError(
                                e.getField(),
                                e.getMessage()
                        )
                )
        );

        return ResponseEntity.badRequest().body(response);
    }

    // 전용 예외가 없는 ResponseStatusException의 기본 응답 (상태 코드는 그대로 유지)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(
            ResponseStatusException e
    ) {
        String message = e.getReason() != null
                ? e.getReason()
                : "요청 처리에 실패했습니다.";

        return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse(
                        "HTTP_ERROR",
                        message,
                        List.of()
                ));
    }

    @ExceptionHandler(NoticeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoticeNotFound(
            NoticeNotFoundException e
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "NOTICE_NOT_FOUND",
                        e.getMessage(),
                        List.of()
                ));
    }

    // 공지는 있으나 저장된 이미지 파일이 없는 경우 -> 404
    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleImageNotFound(
            ImageNotFoundException e
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "IMAGE_NOT_FOUND",
                        e.getMessage(),
                        List.of()
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException e
    ) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        "VALIDATION_ERROR",
                        "요청 값을 확인해 주세요.",
                        List.of(
                                new ErrorResponse.FieldError(
                                        e.getName(),
                                        "입력 형식을 확인해 주세요."
                                )
                        )
                ));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException e
    ) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(new ErrorResponse(
                        "UNSUPPORTED_MEDIA_TYPE",
                        "지원하지 않는 요청 형식입니다.",
                        List.of()
                ));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(
            MaxUploadSizeExceededException e
    ) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponse(
                        "IMAGE_TOO_LARGE",
                        "이미지 용량 제한을 초과했습니다.",
                        List.of()
                ));
    }

    // NoticeImageStorage의 10 MiB 검증에서 던지는 예외 -> 413
    @ExceptionHandler(ImageTooLargeException.class)
    public ResponseEntity<ErrorResponse> handleImageTooLarge(
            ImageTooLargeException e
    ) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponse(
                        "IMAGE_TOO_LARGE",
                        "이미지 용량 제한을 초과했습니다.",
                        List.of()
                ));
    }

    // PNG/JPEG가 아니거나 이미지로 인식할 수 없는 파일 -> 415
    @ExceptionHandler(UnsupportedImageFormatException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedImageFormat(
            UnsupportedImageFormatException e
    ) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(new ErrorResponse(
                        "UNSUPPORTED_MEDIA_TYPE",
                        "지원하지 않는 요청 형식입니다.",
                        List.of()
                ));
    }

    // PNG/JPEG로 인식되지만 손상되어 읽을 수 없는 이미지 -> 422
    @ExceptionHandler(ImageUnreadableException.class)
    public ResponseEntity<ErrorResponse> handleImageUnreadable(
            ImageUnreadableException e
    ) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse(
                        "IMAGE_UNREADABLE",
                        "이미지에서 공지 내용을 읽을 수 없습니다.",
                        List.of()
                ));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBinding(BindException e) {
        List<ErrorResponse.FieldError> errors =
                e.getBindingResult().getFieldErrors().stream()
                        .map(error -> new ErrorResponse.FieldError(
                                error.getField(),
                                error.isBindingFailure()
                                        || error.getDefaultMessage() == null
                                        ? "입력 형식을 확인해 주세요."
                                        : error.getDefaultMessage()
                        ))
                        .toList();

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        "VALIDATION_ERROR",
                        "요청 값을 확인해 주세요.",
                        errors
                ));
    }



    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception e
    ) {
        // 응답에는 내부 정보를 숨기되, 원인 추적을 위해 스택 트레이스는 로그로 남김
        log.error("예상하지 못한 예외가 발생했습니다.", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "INTERNAL_SERVER_ERROR",
                        "요청 처리 중 오류가 발생했습니다.",
                        List.of()
                ));
    }

    @ExceptionHandler(DuplicateLoginIdException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateLoginId(
            DuplicateLoginIdException e
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        "DUPLICATE_LOGIN_ID",
                        e.getMessage(),
                        List.of()
                ));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException e
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        "INVALID_CREDENTIALS",
                        e.getMessage(),
                        List.of()
                ));
    }

    @ExceptionHandler(LectureNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLectureNotFound(
            LectureNotFoundException e
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "LECTURE_NOT_FOUND",
                        e.getMessage(),
                        List.of()
                ));
    }

    @ExceptionHandler(TimetableAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleTimetableAlreadyExists(
            TimetableAlreadyExistsException e
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        "TIMETABLE_ALREADY_EXISTS",
                        e.getMessage(),
                        List.of()
                ));
    }

    // 본문이 비었거나 JSON 형식·타입이 맞지 않을 때 (예: lectureIds에 "abc")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException e
    ) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        "VALIDATION_ERROR",
                        "요청 값을 확인해 주세요.",
                        List.of()
                ));
    }

    @ExceptionHandler(TimetableTimeConflictException.class)
    public ResponseEntity<ErrorResponse> handleTimetableTimeConflict(
            TimetableTimeConflictException e
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        "TIMETABLE_TIME_CONFLICT",
                        e.getMessage(),
                        List.of()
                ));
    }

    // 쿼리 파라미터 검증 실패 (@Validated 사용 시) - 예: academicYear=0, semester=abc
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException e
    ) {
        List<ErrorResponse.FieldError> errors = e.getConstraintViolations().stream()
                .map(v -> {
                    String path = v.getPropertyPath().toString();
                    String field = path.substring(path.lastIndexOf('.') + 1);
                    return new ErrorResponse.FieldError(field, "입력 형식을 확인해 주세요.");
                })
                .toList();

        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", "요청 값을 확인해 주세요.", errors));
    }

    // 쿼리 파라미터 검증 실패 (Spring Boot 3.2+, @Validated 없이 동작할 때)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleMethodValidation(
            HandlerMethodValidationException e
    ) {
        List<ErrorResponse.FieldError> errors = e.getParameterValidationResults().stream()
                .map(r -> new ErrorResponse.FieldError(
                        r.getMethodParameter().getParameterName(),
                        "입력 형식을 확인해 주세요."
                ))
                .toList();

        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", "요청 값을 확인해 주세요.", errors));
    }

    // 필수 쿼리 파라미터 누락 - 예: semester 없이 요청
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException e
    ) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        "VALIDATION_ERROR",
                        "요청 값을 확인해 주세요.",
                        List.of(new ErrorResponse.FieldError(
                                e.getParameterName(),
                                "필수 값입니다."
                        ))
                ));
    }

    @ExceptionHandler(TimetableNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTimetableNotFound(
            TimetableNotFoundException e
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "TIMETABLE_NOT_FOUND",
                        e.getMessage(),
                        List.of()
                ));
    }

    // 외부 AI(Gemini) 호출 실패 (재시도·fallback 후에도 실패) -> 502
    @ExceptionHandler(AiAnalysisFailedException.class)
    public ResponseEntity<ErrorResponse> handleAiAnalysisFailed(
            AiAnalysisFailedException e
    ) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse(
                        "AI_ANALYSIS_FAILED",
                        e.getMessage(),
                        List.of()
                ));
    }

    // 외부 AI(Gemini) 호출 시간 초과 -> 504
    @ExceptionHandler(AiAnalysisTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleAiAnalysisTimeout(
            AiAnalysisTimeoutException e
    ) {
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(new ErrorResponse(
                        "AI_ANALYSIS_TIMEOUT",
                        e.getMessage(),
                        List.of()
                ));
    }

    // 공지 또는 일정이 연결된 시간표 항목 삭제 시도 -> 409
    @ExceptionHandler(TimetableInUseException.class)
    public ResponseEntity<ErrorResponse> handleTimetableInUse(
            TimetableInUseException e
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        "TIMETABLE_IN_USE",
                        e.getMessage(),
                        List.of()
                ));
    }

    // 일정이 없거나 본인 소유가 아닌 경우
    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEventNotFound(
            EventNotFoundException e
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "EVENT_NOT_FOUND",
                        e.getMessage(),
                        List.of()
                ));
    }

    // 빈 강의실 조회: 시작 시각이 종료 시각 이상
    @ExceptionHandler(InvalidTimeRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTimeRange(
            InvalidTimeRangeException e
    ) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        "INVALID_TIME_RANGE",
                        e.getMessage(),
                        List.of()
                ));
    }

    // 빈 강의실 조회: 전달한 건물명이 classrooms에 없음
    @ExceptionHandler(BuildingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBuildingNotFound(
            BuildingNotFoundException e
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "BUILDING_NOT_FOUND",
                        e.getMessage(),
                        List.of()
                ));
    }

    // 빈 강의실 조회: 조회 구간이 내 시간표의 같은 학기·요일 수업과 겹침
    @ExceptionHandler(FreeSlotOverlapsLectureException.class)
    public ResponseEntity<ErrorResponse> handleFreeSlotOverlapsLecture(
            FreeSlotOverlapsLectureException e
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        "FREE_SLOT_OVERLAPS_LECTURE",
                        e.getMessage(),
                        List.of()
                ));
    }

    @ExceptionHandler(SpaceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSpaceNotFound(
            SpaceNotFoundException e
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "SPACE_NOT_FOUND",
                        e.getMessage(),
                        List.of()
                ));
    }
}