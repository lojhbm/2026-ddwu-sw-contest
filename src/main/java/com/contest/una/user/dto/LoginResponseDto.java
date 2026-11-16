package com.contest.una.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDto {
    private String accessToken;
    private String tokenType;
    private Integer expiresIn;
    private UserInfo user;

    @Getter
    @AllArgsConstructor
    public static class UserInfo {

        private final Long userId;
        private final String nickname;
        private final String loginId;
    }

}
