package com.contest.una.global.exception;

// 조회 구간이 내 시간표의 같은 연도·학기·요일 수업과 겹치는 경우 -> 409 FREE_SLOT_OVERLAPS_LECTURE
public class FreeSlotOverlapsLectureException extends RuntimeException {

    public FreeSlotOverlapsLectureException() {
        super("선택한 시간에 등록된 수업이 있습니다.");
    }
}