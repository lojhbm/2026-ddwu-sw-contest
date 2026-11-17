package com.contest.una.global.exception;

// 조회 시작 시각이 종료 시각 이상인 경우 -> 400 INVALID_TIME_RANGE
public class InvalidTimeRangeException extends RuntimeException {

    public InvalidTimeRangeException() {
        super("조회 시간 범위를 확인해 주세요.");
    }
}