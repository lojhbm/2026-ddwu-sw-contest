package com.contest.una.user.controller;

import com.contest.una.user.domain.UserEntity;
import com.contest.una.user.dto.SignupRequestDto;
import com.contest.una.user.dto.SignupResponseDto;
import com.contest.una.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> signup(
            @Valid @RequestBody SignupRequestDto request
    ) {
        UserEntity savedUser = userService.signup(
                request.getLoginId(),
                request.getPassword(),
                request.getNickname()
        );

        String createdAt = savedUser.getCreatedAt()
                .atZone(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        SignupResponseDto response = new SignupResponseDto(
                savedUser.getId(),
                savedUser.getNickname(),
                savedUser.getLoginId(),
                createdAt
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
