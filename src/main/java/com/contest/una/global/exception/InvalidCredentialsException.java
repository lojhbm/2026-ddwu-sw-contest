package com.contest.una.global.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("로그인 정보를 확인해 주세요.");
    }
}
