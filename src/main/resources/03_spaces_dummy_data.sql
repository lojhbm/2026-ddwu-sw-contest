-- 더미데이터: 시설(spaces) - 혼잡도 투표 대상
-- 실행 순서: 01_schema.sql -> 02_lectures_dummy_data.sql -> 03_spaces_dummy_data.sql
-- id는 AUTO_INCREMENT가 아닌 직접 지정 값 (02_lectures_dummy_data.sql과 동일한 방식).
-- 인문관·숭인관은 classrooms의 건물명과 같음 (빈 강의실 조회와 함께 테스트 가능)

INSERT INTO spaces (id, name, building_name) VALUES
    (1, '숭인관 글로벌 라운지', '숭인관'),
    (2, '인문관 1층 라운지', '인문관'),
    (3, '백주년 2층 스터디존', '백주년기념관');