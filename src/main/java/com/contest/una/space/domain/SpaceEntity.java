package com.contest.una.space.domain;

import com.contest.una.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "spaces")
public class SpaceEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "building_name", nullable = false, length = 100)
    private String buildingName;

    @Builder
    public SpaceEntity(String name, String buildingName) {
        this.name = name;
        this.buildingName = buildingName;
    }
}