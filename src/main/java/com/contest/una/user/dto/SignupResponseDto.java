package com.contest.una.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupResponseDto {
    private Long userId;
    private String nickname;
    private String loginId;
    private String createdAt;
}
