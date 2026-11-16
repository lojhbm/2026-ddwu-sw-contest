package com.contest.una.timetable.domain;

import com.contest.una.classroom.domain.ClassroomEntity;
import com.contest.una.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "lectures")
public class LectureEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "course_code", nullable = false, length = 50)
    private String courseCode;

    @Column(name = "section_code", nullable = false, length = 30)
    private String sectionCode;

    @Column(name = "academic_year", nullable = false)
    private Integer academicYear;

    @Column(name = "semester", nullable = false, length = 10)
    private String semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private ClassroomEntity classroom;

    @Column(name = "time_week", nullable = false)
    private Integer timeWeek;

    @Column(name = "time_starts_at", nullable = false)
    private LocalTime timeStartsAt;

    @Column(name = "time_ends_at", nullable = false)
    private LocalTime timeEndsAt;

    @Builder
    public LectureEntity(String courseCode, String name, String sectionCode, Integer academicYear, String semester, ClassroomEntity classroom, Integer timeWeek, LocalTime timeStartsAt, LocalTime timeEndsAt) {
        this.courseCode = courseCode;
        this.name = name;
        this.sectionCode = sectionCode;
        this.academicYear = academicYear;
        this.semester = semester;
        this.classroom = classroom;
        this.timeWeek = timeWeek;
        this.timeStartsAt = timeStartsAt;
        this.timeEndsAt = timeEndsAt;
    }
}
