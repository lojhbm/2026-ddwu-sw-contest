package com.contest.una.global.exception;

public class NoticeNotFoundException extends RuntimeException {

    public NoticeNotFoundException() {
        super("공지를 찾을 수 없습니다.");
    }
}