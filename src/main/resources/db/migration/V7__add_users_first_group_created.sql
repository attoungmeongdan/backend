-- users 테이블에 그룹 도메인용 first_group_created 컬럼 추가
-- 프로덕션 DB에는 이미 수동으로 추가되어 있을 수 있으므로 IF NOT EXISTS 사용 (idempotent)
-- 새 환경 배포 및 DR 상황에서도 안전하게 실행됨

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS first_group_created BOOLEAN NOT NULL DEFAULT FALSE;
