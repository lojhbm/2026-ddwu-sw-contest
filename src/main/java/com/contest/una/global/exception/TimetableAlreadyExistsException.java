package com.contest.una.global.exception;

public class TimetableAlreadyExistsException extends RuntimeException {

    public TimetableAlreadyExistsException() {
        super("이미 등록한 강의가 포함되어 있습니다.");
    }
}