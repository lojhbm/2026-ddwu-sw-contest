package com.contest.una.global.exception;

public class TimetableInUseException extends RuntimeException {
    public TimetableInUseException() {
        super("공지 또는 일정에 연결된 시간표 항목이 있습니다.");
    }
}