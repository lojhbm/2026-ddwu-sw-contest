package com.contest.una.global.exception;

// 전달한 건물명이 classrooms에 존재하지 않는 경우 -> 404 BUILDING_NOT_FOUND
public class BuildingNotFoundException extends RuntimeException {

    public BuildingNotFoundException() {
        super("건물을 찾을 수 없습니다.");
    }
}