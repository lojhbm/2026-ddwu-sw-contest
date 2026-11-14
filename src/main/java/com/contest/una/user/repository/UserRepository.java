package com.contest.una.user.repository;

import com.contest.una.user.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByLoginId(String loginId);
    // loginId로 사용자를 조회할 때 사용.
}