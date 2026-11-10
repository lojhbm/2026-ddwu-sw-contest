package com.contest.una.notice.dto;

import com.contest.una.notice.dto.NoticeListItemResponse;

import java.util.List;

public record NoticeListResponse(
        List<NoticeListItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}



