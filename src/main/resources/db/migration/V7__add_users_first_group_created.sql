-- users 테이블에 그룹 도메인용 first_group_created 컬럼 추가
-- 프로덕션 DB에는 이미 수동으로 추가되어 있을 수 있으므로 IF NOT EXISTS 사용 (idempotent)
-- 신규 환경(CI 등)에서는 Hibernate ddl-auto가 users 테이블을 생성하기 전에 Flyway가 먼저 실행되므로
-- users 테이블이 없을 수 있음 -> to_regclass 로 존재 여부 확인 후 실행

DO $$
BEGIN
    IF to_regclass('public.users') IS NOT NULL THEN
        ALTER TABLE users
            ADD COLUMN IF NOT EXISTS first_group_created BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
END
$$;
