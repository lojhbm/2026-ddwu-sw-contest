package com.contest.una.global.exception;

public class ImageNotFoundException extends RuntimeException {

    public ImageNotFoundException() {
        super("이미지를 찾을 수 없습니다.");
    }
}
