package com.contest.una.global.exception;

import com.contest.una.global.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
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

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(
            ResponseStatusException e
    ) {
        int status = e.getStatusCode().value();

        String code;
        String message;

        if (status == 404 && "NOTICE_NOT_FOUND".equals(e.getReason())) {
            code = "NOTICE_NOT_FOUND";
            message = "공지를 찾을 수 없습니다.";
        } else if (status == 413) {
            code = "IMAGE_TOO_LARGE";
            message = e.getReason() != null
                    ? e.getReason()
                    : "이미지 용량 제한을 초과했습니다.";
        } else {
            code = "HTTP_ERROR";
            message = e.getReason() != null
                    ? e.getReason()
                    : "요청 처리에 실패했습니다.";
        }

        return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse(
                        code,
                        message,
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
}
