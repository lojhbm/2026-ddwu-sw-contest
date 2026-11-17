package com.contest.una.event.domain;

import com.contest.una.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "events")
public class EventEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // 추후 user Entity와 join 예정

    // 출처 공지 ID. 직접 등록했거나 공지가 삭제되면 null
    @Column(name = "source_notice_id")
    private Long sourceNoticeId;

    // timetable.id. null이면 강의와 관련 없는 일정
    @Column(name = "user_lecture_id")
    private Long userLectureId;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    // assignment / exam / other
    @Column(name = "event_type", nullable = false, length = 20)
    private String eventType;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    // null이면 시간 미지정
    @Column(name = "event_time")
    private LocalTime eventTime;

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    @Builder
    public EventEntity(
            Long userId,
            Long sourceNoticeId,
            Long userLectureId,
            String title,
            String eventType,
            LocalDate eventDate,
            LocalTime eventTime,
            String memo
    ) {
        this.userId = userId;
        this.sourceNoticeId = sourceNoticeId;
        this.userLectureId = userLectureId;
        this.title = title;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.memo = memo;
    }
}