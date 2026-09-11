-- 그룹 생성 정책 변경(2인 무료/3~5인 유료)으로 first_group_created 플래그 미사용 처리
-- 프로덕션 DB에 컬럼이 없는 환경에서도 안전하게 실행되도록 IF EXISTS 사용 (idempotent)

ALTER TABLE users
    DROP COLUMN IF EXISTS first_group_created;
