package com.contest.una.global.exception;

public class TimetableTimeConflictException extends RuntimeException {

    public TimetableTimeConflictException() {
        super("시간이 겹치는 강의가 포함되어 있습니다.");
    }
}