package com.contest.una.global.exception;

public class UnsupportedImageFormatException extends RuntimeException {

    public UnsupportedImageFormatException() {
        super("지원하지 않는 요청 형식입니다.");
    }
}