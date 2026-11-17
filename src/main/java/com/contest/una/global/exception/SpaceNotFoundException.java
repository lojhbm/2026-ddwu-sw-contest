package com.contest.una.global.exception;

// 존재하지 않는 시설 -> 404 SPACE_NOT_FOUND
public class SpaceNotFoundException extends RuntimeException {

    public SpaceNotFoundException() {
        super("시설을 찾을 수 없습니다.");
    }
}