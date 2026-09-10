package com.atmd.backend.domain.facility.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

@Slf4j
@Component
@Profile("facility-batch")
@RequiredArgsConstructor
public class FacilityDataBatchRunner implements ApplicationRunner {

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (ResultSet rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM facilities")) {
                rs.next();
                int count = rs.getInt(1);
                if (count > 0) {
                    log.info("[FACILITY_BATCH] 이미 {}건 적재되어 있습니다. 건너뜁니다.", count);
                    return;
                }
            }

            log.info("[FACILITY_BATCH] 시설 데이터 적재를 시작합니다.");
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("db/batch/facilities_data.sql"));

            try (ResultSet rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM facilities")) {
                rs.next();
                log.info("[FACILITY_BATCH] 적재 완료. {}건 삽입됨.", rs.getInt(1));
            }
        }
    }
}
