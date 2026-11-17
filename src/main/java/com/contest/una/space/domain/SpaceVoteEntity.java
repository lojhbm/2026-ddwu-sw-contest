package com.contest.una.space.domain;

import com.contest.una.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 시설 혼잡도 투표. 재투표는 기존 행을 고치지 않고 새 행으로 저장하며,
// 집계할 때 사용자별 최신 1표(createdAt 기준)만 유효 투표로 사용한다.
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "space_votes")
public class SpaceVoteEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "space_id", nullable = false)
    private Long spaceId;

    // low / medium / high
    @Column(name = "congestion_level", nullable = false, length = 10)
    private String congestionLevel;

    @Builder
    public SpaceVoteEntity(Long userId, Long spaceId, String congestionLevel) {
        this.userId = userId;
        this.spaceId = spaceId;
        this.congestionLevel = congestionLevel;
    }
}