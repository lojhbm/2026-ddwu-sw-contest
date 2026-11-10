package com.contest.una.global.exception;

public class InvalidRequestException extends RuntimeException {

    private final String field;

    public InvalidRequestException(String field, String reason) {
        super(reason);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
