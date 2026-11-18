package com.contest.una.event.domain;

import com.contest.una.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 과제 캡처 원본 이미지. 여러 일정이 같은 이미지를 공유할 수 있음
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "capture_images")
public class CaptureImageEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Builder
    public CaptureImageEntity(Long userId, String imagePath) {
        this.userId = userId;
        this.imagePath = imagePath;
    }
}
