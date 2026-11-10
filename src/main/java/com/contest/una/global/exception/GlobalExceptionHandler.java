package com.contest.una.global.exception;

import com.contest.una.global.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}
