-- 그룹 페널티 기능 제거로 penalty 컬럼 삭제
-- 프로덕션 DB에 컬럼이 없는 환경에서도 안전하게 실행되도록 IF EXISTS 사용 (idempotent)
-- CI/신규 환경에서 groups 테이블이 아직 없을 수 있으므로 to_regclass 로 존재 여부 확인

DO $$
BEGIN
    IF to_regclass('public.groups') IS NOT NULL THEN
        ALTER TABLE groups
            DROP COLUMN IF EXISTS penalty;
    END IF;
END
$$;
