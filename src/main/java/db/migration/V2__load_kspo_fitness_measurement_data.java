package db.migration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

public class V2__load_kspo_fitness_measurement_data extends BaseJavaMigration {

    private static final Logger log =
            LoggerFactory.getLogger(V2__load_kspo_fitness_measurement_data.class);
    private static final String DATA_RESOURCE =
            "opendata/KS_NFA_FTNESS_MESURE_MVN_PRSCRPTN_GNRLZ_INFO_202606.json";
    private static final int BATCH_SIZE = 1_000;
    private static final DateTimeFormatter SOURCE_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private static final String[] ITEM_CODES = {
            "019", "023"
    };

    private static final String INSERT_SQL = createInsertSql();

    @Override
    public void migrate(Context context) throws Exception {
        long startedAt = System.currentTimeMillis();
        int processedCount = 0;
        int insertedCount = 0;
        ObjectMapper objectMapper = new ObjectMapper();

        log.info("KSPO 체력측정 초기 데이터 적재를 시작합니다. resource={}", DATA_RESOURCE);

        try (InputStream inputStream = openDataResource();
             JsonParser parser = objectMapper.getFactory().createParser(inputStream);
             PreparedStatement statement = context.getConnection().prepareStatement(INSERT_SQL)) {
            requireArray(parser);

            int pendingBatchSize = 0;
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                JsonNode row = objectMapper.readTree(parser);
                bindRow(statement, row);
                statement.addBatch();
                pendingBatchSize++;
                processedCount++;

                if (pendingBatchSize == BATCH_SIZE) {
                    insertedCount += countInsertedRows(statement.executeBatch());
                    pendingBatchSize = 0;
                    log.info("KSPO 체력측정 초기 데이터 적재 중입니다. processed={}, inserted={}, skipped={}",
                            processedCount, insertedCount, processedCount - insertedCount);
                }
            }

            if (pendingBatchSize > 0) {
                insertedCount += countInsertedRows(statement.executeBatch());
            }
        }

        log.info("KSPO 체력측정 초기 데이터 적재를 완료했습니다. processed={}, inserted={}, "
                        + "skipped={}, elapsedMs={}",
                processedCount, insertedCount, processedCount - insertedCount,
                System.currentTimeMillis() - startedAt);
    }

    private InputStream openDataResource() {
        InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(DATA_RESOURCE);
        if (inputStream == null) {
            throw new FlywayException("KSPO 초기 데이터 파일을 찾을 수 없습니다: " + DATA_RESOURCE);
        }
        return inputStream;
    }

    private void requireArray(JsonParser parser) throws IOException {
        if (parser.nextToken() != JsonToken.START_ARRAY) {
            throw new FlywayException("KSPO 초기 데이터의 최상위 JSON 형식은 배열이어야 합니다.");
        }
    }

    private void bindRow(PreparedStatement statement, JsonNode row) throws SQLException {
        int parameter = 1;
        statement.setString(parameter++, requiredText(row, "MBER_SEQ_NO_VALUE"));
        statement.setInt(parameter++, requiredInteger(row, "MESURE_SEQ_NO"));
        statement.setString(parameter++, requiredText(row, "CNTER_NM"));
        statement.setString(parameter++, requiredText(row, "AGRDE_FLAG_NM"));
        statement.setString(parameter++, requiredText(row, "MESURE_PLACE_FLAG_NM"));
        statement.setInt(parameter++, requiredInteger(row, "MESURE_AGE_CO"));
        statement.setString(parameter++, requiredText(row, "INPT_FLAG_NM"));
        setNullableText(statement, parameter++, row, "CRTFC_FLAG_NM");
        statement.setDate(parameter++, Date.valueOf(LocalDate.parse(
                requiredText(row, "MESURE_DE"), SOURCE_DATE_FORMAT)));
        statement.setString(parameter++, requiredText(row, "SEXDSTN_FLAG_CD"));

        for (String itemCode : ITEM_CODES) {
            setNullableDecimal(statement, parameter++, row, "MESURE_IEM_" + itemCode + "_VALUE");
        }
        setNullableText(statement, parameter, row, "MVM_PRSCRPTN_CN");
    }

    private String requiredText(JsonNode row, String fieldName) {
        JsonNode value = row.get(fieldName);
        if (value == null || value.isNull() || value.asText().isBlank()) {
            throw new FlywayException("KSPO 초기 데이터 필수 필드가 비어 있습니다: " + fieldName);
        }
        return value.asText();
    }

    private int requiredInteger(JsonNode row, String fieldName) {
        String value = requiredText(row, fieldName);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new FlywayException("KSPO 초기 데이터의 정수 형식이 잘못되었습니다: "
                    + fieldName + "=" + value, exception);
        }
    }

    private void setNullableText(
            PreparedStatement statement,
            int parameter,
            JsonNode row,
            String fieldName
    ) throws SQLException {
        JsonNode value = row.get(fieldName);
        if (value == null || value.isNull() || value.asText().isBlank()) {
            statement.setNull(parameter, Types.VARCHAR);
            return;
        }
        statement.setString(parameter, value.asText());
    }

    private void setNullableDecimal(
            PreparedStatement statement,
            int parameter,
            JsonNode row,
            String fieldName
    ) throws SQLException {
        JsonNode value = row.get(fieldName);
        if (value == null || value.isNull() || value.asText().isBlank()) {
            statement.setNull(parameter, Types.NUMERIC);
            return;
        }

        try {
            statement.setBigDecimal(parameter, new BigDecimal(value.asText()));
        } catch (NumberFormatException exception) {
            throw new FlywayException("KSPO 초기 데이터의 숫자 형식이 잘못되었습니다: "
                    + fieldName + "=" + value.asText(), exception);
        }
    }

    private int countInsertedRows(int[] updateCounts) {
        int insertedCount = 0;
        for (int updateCount : updateCounts) {
            if (updateCount == PreparedStatement.SUCCESS_NO_INFO) {
                insertedCount++;
            } else if (updateCount > 0) {
                insertedCount += updateCount;
            }
        }
        return insertedCount;
    }

    private static String createInsertSql() {
        String itemColumns = Arrays.stream(ITEM_CODES)
                .map(code -> "measurement_item_" + code + "_value")
                .collect(Collectors.joining(", "));
        int parameterCount = 10 + ITEM_CODES.length + 1;
        String placeholders = String.join(", ", java.util.Collections.nCopies(parameterCount, "?"));

        return "INSERT INTO kspo_fitness_measurement ("
                + "member_sequence_value, measurement_sequence_no, center_name, age_group_name, "
                + "measurement_place_type_name, measurement_age, input_type_name, "
                + "certification_grade_name, measurement_date, gender_code, "
                + itemColumns + ", movement_prescription_content, imported_at) VALUES ("
                + placeholders + ", CURRENT_TIMESTAMP) ON CONFLICT (member_sequence_value, "
                + "measurement_sequence_no, "
                + "measurement_date) DO NOTHING";
    }
}
