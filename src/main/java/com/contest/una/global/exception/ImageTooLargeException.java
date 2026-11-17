package com.contest.una.global.exception;

public class ImageTooLargeException extends RuntimeException {

    public ImageTooLargeException() {
        super("이미지 용량 제한을 초과했습니다.");
    }
}