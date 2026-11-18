# 2026-ddwu-sw-contest
2026 동덕여대 컴퓨터학과 주최 SW 경진대회

# 실행 방법
```
# 1. DB 준비 (MySQL): 스키마와 더미데이터 순서대로 실행
#    01_schema.sql -> 02_lectures_dummy_data.sql -> 03_spaces_dummy_data.sql

# 2. 환경변수 설정
export DB_URL=jdbc:mysql://localhost:3306/<db>
export DB_USERNAME=<user>
export DB_PASSWORD=<password>
export JWT_SECRET=<충분히 긴 비밀키>
export GEMINI_API_KEY=<발급받은 키>

# 3. 실행
./gradlew bootRun
```
