package com.contest.una.global.exception;

public class ImageUnreadableException extends RuntimeException {

    public ImageUnreadableException() {
        super("이미지에서 공지 내용을 읽을 수 없습니다.");
    }

    public ImageUnreadableException(Throwable cause) {
        super("이미지에서 공지 내용을 읽을 수 없습니다.", cause);
    }
}