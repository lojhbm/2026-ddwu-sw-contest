package com.contest.una.user.service;

import com.contest.una.global.exception.DuplicateLoginIdException;
import com.contest.una.user.domain.UserEntity;
import com.contest.una.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional
    public UserEntity signup(String loginId, String password, String nickname) {

        // 아이디 중복 확인
        if (userRepository.findByLoginId(loginId).isPresent()) {
            throw new DuplicateLoginIdException();
        }

        String encodedPassword = passwordEncoder.encode(password);

        UserEntity user = new UserEntity(loginId, encodedPassword, nickname);

        return userRepository.save(user); // save 메서드: user가 저장된 entity 반환
    }

}
