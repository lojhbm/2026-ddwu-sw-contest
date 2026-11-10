package com.contest.una.notice.domain;

import com.contest.una.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notices")
public class NoticeEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // 추후 user Entity와 join 예정

    @Column(name = "user_lecture_id", length = 100)
    private Long userLectureId;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "image_path", nullable = false, length = 500)
    private String imagePath;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_items", nullable = false, columnDefinition = "json") // db에는 리스트를 json 타입으로 저장
    private List<String> actionItems = new ArrayList<>();
}
