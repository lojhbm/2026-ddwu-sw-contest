package com.contest.una.classroom.domain;

import com.contest.una.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "classrooms")
public class ClassroomEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "building_name", nullable = false, length = 100)
    private String buildingName;

    @Column(name = "room_name", nullable = false, length = 50)
    private String roomName;

    @Builder
    public ClassroomEntity(String buildingName, String roomName) {
        this.buildingName = buildingName;
        this.roomName = roomName;
    }
}
