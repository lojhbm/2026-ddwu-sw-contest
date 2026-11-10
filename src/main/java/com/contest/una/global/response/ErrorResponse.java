package com.contest.una.global.response;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<FieldError> errors
) {
    public record FieldError(
            String field,
            String reason
    ) {}
}
