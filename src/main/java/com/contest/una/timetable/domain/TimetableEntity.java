package com.contest.una.timetable.domain;

import com.contest.una.global.entity.BaseTimeEntity;
import com.contest.una.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "timetable",
        uniqueConstraints = @UniqueConstraint(
                name = "UQ_TIMETABLE_USER_LECTURE",
                columnNames = {"user_id", "lecture_id"}))
public class TimetableEntity extends BaseTimeEntity {
    private static final String DEFAULT_COLOR = "gray";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private LectureEntity lecture;

    // 시간표와 캘린더에서 과목을 구분할 색
    @Column(name = "color", nullable = false, length = 50)
    private String color;

    @Builder
    public TimetableEntity(UserEntity user, LectureEntity lecture, String color) {
        this.user = user;
        this.lecture = lecture;
        this.color = (color == null) ? DEFAULT_COLOR : color;
    }

    public void updateColor(String color) {
        this.color = color;
    }
}
