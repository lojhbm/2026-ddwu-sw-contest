-- ============================================================
-- 스키마 초기화 + 테이블 생성 (MySQL)
-- ※ 1단계는 기존 테이블과 데이터를 모두 삭제합니다. 개발 DB에서만 실행하세요.
-- ============================================================

-- ------------------------------------------------------------
-- 1. 기존 테이블 삭제 (자식 -> 부모 순서, FK 추가 시에도 안전하도록 검사 해제)
-- ------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `space_votes`;
DROP TABLE IF EXISTS `notices`;
DROP TABLE IF EXISTS `events`;
DROP TABLE IF EXISTS `timetable`;
DROP TABLE IF EXISTS `lectures`;
DROP TABLE IF EXISTS `classrooms`;
DROP TABLE IF EXISTS `spaces`;
DROP TABLE IF EXISTS `users`;

SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- 2. 테이블 생성 (id는 AUTO_INCREMENT)
-- ------------------------------------------------------------
CREATE TABLE `users` (
                         `id`         BIGINT       NOT NULL AUTO_INCREMENT,
                         `nickname`   VARCHAR(100) NOT NULL,
                         `login_id`   VARCHAR(50)  NOT NULL,
                         `password`   VARCHAR(255) NOT NULL,
                         `created_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         `updated_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT `PK_USERS` PRIMARY KEY (`id`)
);

CREATE TABLE `classrooms` (
                              `id`            BIGINT       NOT NULL AUTO_INCREMENT,
                              `building_name` VARCHAR(100) NOT NULL COMMENT '건물명',
                              `room_number`   VARCHAR(50)  NOT NULL COMMENT '강의실 번호',
                              `created_at`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              `updated_at`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT `PK_CLASSROOMS` PRIMARY KEY (`id`)
);

CREATE TABLE `lectures` (
                            `id`             BIGINT       NOT NULL AUTO_INCREMENT,
                            `name`           VARCHAR(200) NOT NULL COMMENT '강의명',
                            `course_code`    VARCHAR(50)  NOT NULL COMMENT '과목 코드',
                            `section_code`   VARCHAR(30)  NOT NULL COMMENT '분반 코드',
                            `academic_year`  SMALLINT     NOT NULL COMMENT '개설 연도',
                            `semester`       VARCHAR(10)  NOT NULL COMMENT '1: 1학기 / summer: 여름 / 2: 2학기 / winter: 겨울',
                            `classroom_id`   BIGINT       NULL     COMMENT '온라인 수업 또는 강의실 미정이면 NULL',
                            `time_week`      SMALLINT     NOT NULL COMMENT '월요일 1 ~ 일요일 7',
                            `time_starts_at` TIME         NOT NULL COMMENT '수업 시작 시각',
                            `time_ends_at`   TIME         NOT NULL COMMENT '수업 종료 시각',
                            `created_at`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `updated_at`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT `PK_LECTURES` PRIMARY KEY (`id`)
);

CREATE TABLE `timetable` (
                             `id`         BIGINT      NOT NULL AUTO_INCREMENT,
                             `user_id`    BIGINT      NOT NULL,
                             `lecture_id` BIGINT      NOT NULL,
                             `color`      VARCHAR(50) NOT NULL DEFAULT 'gray' COMMENT '시간표와 캘린더에서 과목을 구분할 색',
                             `created_at` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             `updated_at` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT `PK_TIMETABLE` PRIMARY KEY (`id`)
);

CREATE TABLE `events` (
                          `id`               BIGINT       NOT NULL AUTO_INCREMENT,
                          `user_id`          BIGINT       NOT NULL,
                          `source_notice_id` BIGINT       NULL COMMENT '출처 공지 ID. 직접 등록한 일정은 NULL',
                          `user_lecture_id`  BIGINT       NULL COMMENT '강의별 일정 필터링을 위함. NULL이면 강의 관련 일정X',
                          `title`            VARCHAR(300) NOT NULL,
                          `event_type`       VARCHAR(20)  NOT NULL COMMENT 'assignment: 과제 / exam: 시험 / other: 기타',
                          `event_date`       DATE         NOT NULL,
                          `event_time`       TIME         NULL COMMENT 'NULL이면 시간 미지정',
                          `memo`             TEXT         NULL,
                          `created_at`       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          `updated_at`       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT `PK_EVENTS` PRIMARY KEY (`id`)
);

CREATE TABLE `spaces` (
                          `id`            BIGINT       NOT NULL AUTO_INCREMENT,
                          `name`          VARCHAR(100) NOT NULL,
                          `building_name` VARCHAR(100) NOT NULL COMMENT '건물명',
                          `created_at`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          `updated_at`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT `PK_SPACES` PRIMARY KEY (`id`)
);

CREATE TABLE `space_votes` (
                               `id`               BIGINT      NOT NULL AUTO_INCREMENT,
                               `user_id`          BIGINT      NOT NULL,
                               `space_id`         BIGINT      NOT NULL,
                               `congestion_level` VARCHAR(10) NOT NULL COMMENT 'low: 여유 / medium: 보통 / high: 혼잡',
                               `created_at`       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               `updated_at`       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정 가능 여부 논의 필요',
                               CONSTRAINT `PK_SPACE_VOTES` PRIMARY KEY (`id`)
);

CREATE TABLE `notices` (
                           `id`               BIGINT       NOT NULL AUTO_INCREMENT,
                           `user_id`          BIGINT       NOT NULL,
                           `user_lecture_id`  BIGINT       NULL,
                           `title`            VARCHAR(300) NOT NULL,
                           `summary`          TEXT         NOT NULL COMMENT '공지사항 요약',
                           `image_path`       VARCHAR(500) NULL COMMENT '공지 사진, 파일 경로 저장',
                           `action_items`     JSON         NOT NULL DEFAULT (JSON_ARRAY()) COMMENT '핵심 목록',
                           `event_candidates` JSON         NULL COMMENT '일정 후보 목록',
                           `created_at`       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           `updated_at`       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT `PK_NOTICES` PRIMARY KEY (`id`)
);

-- ------------------------------------------------------------
-- 3. UNIQUE / FK
-- ------------------------------------------------------------
ALTER TABLE `users`      ADD CONSTRAINT `UQ_USERS_LOGIN_ID`        UNIQUE (`login_id`);
ALTER TABLE `timetable`  ADD CONSTRAINT `UQ_TIMETABLE_USER_LECTURE` UNIQUE (`user_id`, `lecture_id`);
--
ALTER TABLE `lectures`    ADD CONSTRAINT `FK_LECTURES_CLASSROOM`      FOREIGN KEY (`classroom_id`)     REFERENCES `classrooms` (`id`);
ALTER TABLE `timetable`   ADD CONSTRAINT `FK_TIMETABLE_USER`          FOREIGN KEY (`user_id`)          REFERENCES `users` (`id`);
ALTER TABLE `timetable`   ADD CONSTRAINT `FK_TIMETABLE_LECTURE`       FOREIGN KEY (`lecture_id`)       REFERENCES `lectures` (`id`);
ALTER TABLE `events`      ADD CONSTRAINT `FK_EVENTS_USER`             FOREIGN KEY (`user_id`)          REFERENCES `users` (`id`);
ALTER TABLE `events`      ADD CONSTRAINT `FK_EVENTS_NOTICE`           FOREIGN KEY (`source_notice_id`) REFERENCES `notices` (`id`);
ALTER TABLE `events`      ADD CONSTRAINT `FK_EVENTS_USER_LECTURE`     FOREIGN KEY (`user_lecture_id`)  REFERENCES `timetable` (`id`);
ALTER TABLE `notices`     ADD CONSTRAINT `FK_NOTICES_USER`            FOREIGN KEY (`user_id`)          REFERENCES `users` (`id`);
ALTER TABLE `notices`     ADD CONSTRAINT `FK_NOTICES_USER_LECTURE`    FOREIGN KEY (`user_lecture_id`)  REFERENCES `timetable` (`id`);
ALTER TABLE `space_votes` ADD CONSTRAINT `FK_SPACE_VOTES_USER`        FOREIGN KEY (`user_id`)          REFERENCES `users` (`id`);
ALTER TABLE `space_votes` ADD CONSTRAINT `FK_SPACE_VOTES_SPACE`       FOREIGN KEY (`space_id`)         REFERENCES `spaces` (`id`);
