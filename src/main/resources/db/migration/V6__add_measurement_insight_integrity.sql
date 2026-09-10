ALTER TABLE measurement_insight
    ADD COLUMN generation_status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED';

ALTER TABLE measurement_insight
    ADD CONSTRAINT ck_measurement_insight_generation_status
        CHECK (generation_status IN ('GENERATING', 'COMPLETED'));

DO $$
BEGIN
    IF to_regclass('public.users') IS NOT NULL THEN
        ALTER TABLE measurement_insight
            ADD CONSTRAINT fk_measurement_insight_user
                FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
    END IF;
END
$$;
