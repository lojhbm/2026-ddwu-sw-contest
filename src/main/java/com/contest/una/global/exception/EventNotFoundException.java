package com.contest.una.global.exception;

// 일정이 없거나 본인 소유가 아닌 경우 -> 404 (타인의 일정도 존재 여부를 드러내지 않음)
public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException() {
        super("일정을 찾을 수 없습니다.");
    }
}