package com.contest.una.event.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 일정 목록 조회 응답. 결과가 없으면 items는 빈 리스트
@Getter
@Builder
public class EventListResponseDto {
    private List<EventResponse> items;
}