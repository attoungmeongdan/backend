package com.atmd.backend.domain.facility.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@Profile("facility-batch")
@RequiredArgsConstructor
public class FacilityDataBatchRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM facilities", Integer.class);
        if (count != null && count > 0) {
            log.info("[FACILITY_BATCH] 이미 {}건 적재되어 있습니다. 건너뜁니다.", count);
            return;
        }

        log.info("[FACILITY_BATCH] 시설 데이터 적재를 시작합니다.");
        ClassPathResource resource = new ClassPathResource("db/batch/facilities_data.sql");
        String sql = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int inserted = jdbcTemplate.update(sql);
        log.info("[FACILITY_BATCH] 적재 완료. {}건 삽입됨.", inserted);
    }
}
