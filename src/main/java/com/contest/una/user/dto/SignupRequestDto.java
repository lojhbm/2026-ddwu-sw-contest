package com.contest.una.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequestDto {

    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Size(min = 1, max = 100, message = "닉네임은 1~100자여야 합니다.")
    private String nickname;

    @NotBlank(message = "로그인 ID를 입력해 주세요.")
    @Pattern(
            regexp = "^[a-z0-9_]{4,50}$",
            message = "로그인 ID는 영문 소문자, 숫자, 밑줄로 구성된 4~50자여야 합니다."
    )
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,20}$",
            message = "비밀번호는 영문과 숫자를 포함한 8~20자여야 합니다."
    )
    private String password;

    public SignupRequestDto(
            String nickname,
            String loginId,
            String password
    ) {
        setNickname(nickname);
        this.loginId = loginId;
        this.password = password;
    }

    // 닉네임 앞뒤 공백 제거
    public void setNickname(String nickname) {
        this.nickname = nickname == null ? null : nickname.strip();
    }
}
